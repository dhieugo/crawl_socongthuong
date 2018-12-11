import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class SoCongThuong {

    public static void main(String [] args) throws Exception {

        SoCongThuong sct = new SoCongThuong();

        List<Office> officeList = new ArrayList<>();
        List<Integer> ids = sct.findIds();

        ids.forEach(i -> {
            try {
                Office office = sct.findOfficeById(i);
                officeList.add(office);
                Thread.sleep(10);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("congthuong.csv"), "UTF-8"));
        CSVPrinter csvPrinter = new CSVPrinter(bw, CSVFormat.EXCEL.withHeader(
                "Tên văn phòng đại diện",
                "Ngày cấp",
                "Ngày hết hạn",
                "Quốc gia",
                "Điện thoại VPDD",
                "Địa chỉ",
                "Tên trưởng đại diện",
                "Quốc tịch",
                "Số giấy phép",
                "Ngày cấp phép",
                "Ngày giấy phép hết hạn",
                "Lĩnh vực hoạt động",
                "Tên công ty mẹ",
                "Địa chỉ van phong chinh",
                "Điện thoại VPDD Chinh",
                "Năm thành lập",
                "Nơi ĐKTL",
                "Bang, Tỉnh, TP",
                "Vốn điều lệ")
                .withDelimiter(',')
                .withQuote('"')
                .withRecordSeparator("\r\n"));

        for (Office of: officeList) {
            csvPrinter.printRecord(
                    of.getName(),
                    of.getIssueDate(),
                    of.getExpiryDate(),
                    of.getNation(),
                    of.getPhone(),
                    of.getAddress(),
                    of.getChiefRepresentative(),
                    of.getNation(),
                    of.getLicenseNumber(),
                    of.getLicenseIssueDate(),
                    of.getLicenseExpiryDate(),
                    of.getLinhvuc(),
                    of.getParentCompanyName(),
                    of.getParentCompanyAddress(),
                    of.getParentCompanyPhone(),
                    of.getParentCreatedDate(),
                    of.getParentNation(),
                    of.getParentCity(),
                    of.getParentMoney()
            );
        }

        csvPrinter.flush();
    }

    public Office findOfficeById(int id) throws IOException {

        Office office = new Office();

        String link = "http://congthuong.hochiminhcity.gov.vn/van-phong-dai-dien";
        Map<String, String> params = new HashMap<>();

        params.put("p_p_id", "10_WAR_dvcportlet");
        params.put("p_p_lifecycle", "0");
        params.put("p_p_state", "exclusive");
        params.put("_10_WAR_dvcportlet_vanPhongDaiDienId", id + "");
        params.put("_10_WAR_dvcportlet_mvcPath", "/html/portlet/dulieuchuyennganh/van_phong_dai_dien/detail.jsp");

        StringBuilder url = new StringBuilder(link);
        url.append("?");
        // generate parameter from map
        for (Map.Entry<String, String> entry: params.entrySet()) {
            url.append(String.format("%s=%s&", entry.getKey(), entry.getValue()));
        }
        System.out.println(url.toString());
        Document doc = Jsoup.connect(url.toString()).post();

        Elements tds = doc.select("table.info");
        // find Tên văn phòng đại diện:
        Elements nameElements = tds.get(0).select("tr:first-child td:nth-child(2)");
        office.setName(nameElements.html());

        // find ngay cap
        Elements ngaycapElements = tds.get(0).select("tr:nth-child(2)");
        office.setIssueDate(ngaycapElements.select("td:nth-child(2)").html());
        office.setExpiryDate(ngaycapElements.select("td:nth-child(4)").html());

        // find quoc gia

        Elements nationAndPhone = tds.get(0).select("tr:nth-child(3)");
        office.setNation(nationAndPhone.select("td:nth-child(2)").html());
        office.setPhone(nationAndPhone.select("td:nth-child(4)").html());

        // find address
        Elements address = tds.get(0).select("tr:nth-child(4)");
        office.setAddress(address.select("td:nth-child(2)").html());

        // find dai dien
        Elements daidienElements = tds.get(0).select("tr:nth-child(5)");
        office.setChiefRepresentative(daidienElements.select("td:nth-child(2)").html());
        office.setNation(daidienElements.select("td:nth-child(4)").html());

        // find all thong tin cua giay phep
        Elements giayphepElements = tds.get(1).select("tr:nth-child(1)");
        office.setLicenseNumber(giayphepElements.select("td:nth-child(2)").html());
        office.setLicenseIssueDate(giayphepElements.select("td:nth-child(4)").html());

        Elements giayphepElements2 = tds.get(1).select("tr:nth-child(2)");
        office.setLicenseExpiryDate(giayphepElements2.select("td:nth-child(2)").html());

        // find linh vuc
        Elements linhvucElements = tds.get(1).select("tr:nth-child(3)");
        Elements linhvucItems = linhvucElements.select("tbody");
        String linhvuc = linhvucItems.stream()
                .map(lv -> lv.select("td:nth-child(2)").html() + " : " + lv.select("td:nth-child(3)").html() + ",")
                .collect(Collectors.joining());
        office.setLinhvuc(linhvuc);

        // find parent office
        Elements parentOfficeElements = tds.get(2).select("tbody");

        office.setParentCompanyName(parentOfficeElements.select("tr:nth-child(1) td:nth-child(2)").first().html());
        office.setParentCompanyAddress(parentOfficeElements.select("tr:nth-child(2) td:nth-child(2)").first().html());
        office.setParentCompanyPhone(parentOfficeElements.select("tr:nth-child(3) td:nth-child(2)").first().html());
        office.setParentCreatedDate(parentOfficeElements.select("tr:nth-child(3) td:nth-child(4)").first().html());
        office.setParentNation(parentOfficeElements.select("tr:nth-child(4) td:nth-child(2)").first().html());
        office.setParentCity(parentOfficeElements.select("tr:nth-child(4) td:nth-child(4)").first().html());
        office.setParentMoney(parentOfficeElements.select("tr:nth-child(5) td:nth-child(2)").first().html());
        return office;
    }

    public List<Integer> findIds() throws IOException {

        List<Integer> allIds = new ArrayList<>();
        System.out.println("Start to search at: " + System.currentTimeMillis());

        BufferedWriter writer = new BufferedWriter(new FileWriter("congthuong_ids.txt"));
        PrintWriter printWriter = new PrintWriter(writer);

        String rootLink = "http://congthuong.hochiminhcity.gov.vn/van-phong-dai-dien";
        int page = 1;
        List<Integer> ids = findAllIds(rootLink, page);

        while (ids.isEmpty() == false) {
            allIds.addAll(ids);
            ids.forEach((i) -> {
                printWriter.printf(i + "\n");
            });
            page++;
            ids = findAllIds(rootLink, page);
        }

        // close file
        printWriter.close();

        System.out.println("Finish search/write at: " + System.currentTimeMillis());

        return allIds;
    }

    public List<Integer> findAllIds(String rootLink, int page) throws IOException {
        List<Integer> idList = new ArrayList<>();

        Map<String, String> params = new HashMap<>();

        params.put("p_p_id", "10_WAR_dvcportlet");
        params.put("p_p_lifecycle", "0");
        params.put("p_p_state", "normal");
        params.put("p_p_mode", "view");
        params.put("_10_WAR_dvcportlet_mvcPath", "/html/portlet/dulieuchuyennganh/van_phong_dai_dien/vanphongdaidien.jsp");
        params.put("_10_WAR_dvcportlet_delta", "20");
        params.put("_10_WAR_dvcportlet_keywords", "");
        params.put("_10_WAR_dvcportlet_advancedSearch", "false");
        params.put("_10_WAR_dvcportlet_andOperator", "true");
        params.put("_10_WAR_dvcportlet_tenTiengViet", "");
        params.put("_10_WAR_dvcportlet_truongDaiDien", "");
        params.put("_10_WAR_dvcportlet_soGiayPhep", "");
        params.put("_10_WAR_dvcportlet_tuNgay", "16/11/2010");
        params.put("_10_WAR_dvcportlet_denNgay", "01/01/2019");
        params.put("_10_WAR_dvcportlet_thanhPho", "");
        params.put("_10_WAR_dvcportlet_quanHuyen", "");
        params.put("_10_WAR_dvcportlet_phuong", "");
        params.put("_10_WAR_dvcportlet_duong", "");
        params.put("_10_WAR_dvcportlet_columnSoGiayPhep", "false");
        params.put("_10_WAR_dvcportlet_orderByType", "desc");
        params.put("_10_WAR_dvcportlet_resetCur", "false");
        params.put("cur", "Trang " + page + "");

        StringBuilder url = new StringBuilder(rootLink);
        url.append("?");
        // generate parameter from map
        for (Map.Entry<String, String> entry: params.entrySet()) {
            url.append(String.format("%s=%s&", entry.getKey(), entry.getValue()));
        }
        System.out.println(url.toString());
        Document doc = Jsoup.connect(url.toString()).post();
        Elements table = doc.select("div.search-container-vpdaidien table.taglib-search-iterator");

        // find all IDs

        Elements ids = table.select("tr.results-row td:nth-child(2) a");

        ids.forEach((id) -> {
            String attr = id.attr("href");
            attr = attr.replaceAll("javaScript:_10_WAR_dvcportlet_detail", "");
            attr = attr.replaceAll("\\(", "");
            attr = attr.replaceAll("\\)", "");
            attr = attr.replaceAll(";", "");

            idList.add(Integer.parseInt(attr));
        });

        return idList;
    }
}
