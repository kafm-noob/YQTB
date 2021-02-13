import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


class HttpUtil {

    private static String http(String url, String method, Map<String, String> params, Map<String, String> properties) {
        URL u = null;
        HttpURLConnection con = null;

        StringBuilder sb = new StringBuilder();
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                sb.append(e.getKey());
                sb.append("=");
                sb.append(e.getValue());
                sb.append("&");
            }
            sb.deleteCharAt(sb.length()-1);
        }

        // 尝试发送请求
        try {
            u = new URL(url);
            con = (HttpURLConnection) u.openConnection();
            con.setReadTimeout(10 * 1000);
            con.setRequestMethod(method);
            if("GET".equals(method)) con.setDoOutput(false);
            if("POST".equals(method)) con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);

            if (properties != null) {
                for (String key : properties.keySet()) {
                    con.setRequestProperty(key, properties.get(key));
                }
            }

            if("POST".equals(method)){
                OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8);
                osw.write(sb.toString());
                osw.flush();
                osw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder buffer = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String temp;
            while ((temp = br.readLine()) != null) {
                buffer.append(temp);
                buffer.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer.toString();
    }

    public static String post(String url, Map<String, String> params, Map<String, String> properties) {
        return http(url, "POST", params, properties);
    }

    public static String get(String url, Map<String, String> params, Map<String, String> properties) {
        return http(url, "GET", params, properties);
    }
}

public class YQTB {
    private static String COOKIE;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36 Edg/88.0.705.63";
    private static final String Referer1 = "http://xgb.ahstu.edu.cn/SPCP/Web/Account/ChooseSys";
    private static final String Referer2 = "http://xgb.ahstu.edu.cn/SPCP/Web/Temperature/StuTemperatureInfo";

    private static final String temperatureURL = "http://xgb.ahstu.edu.cn/SPCP/Web/Temperature/StuTemperatureInfo";

    private static Logger logger = Logger.getLogger("温度填报监控");

    public static void main(String[] args) {
        
        COOKIE = args[0];
        
        //Temperature
        Map<String, String> properties = new HashMap<>();
        properties.put("User-Agent", USER_AGENT);
        properties.put("Cookie", COOKIE);
        properties.put("Referer", Referer1);


        String resp = HttpUtil.get(temperatureURL, null, properties);

        String regString = "<input name=\"ReSubmiteFlag\" type=\"hidden\" value=\"";
        int leftIndex = resp.indexOf(regString) + regString.length();
        if(leftIndex == -1 + regString.length()) {
            System.exit(0);
            logger.warning("获取ReSubmiteFlag 失败");
        }
        int rightIndex = resp.indexOf("\"", leftIndex);
        String ReSubmiteFlag = resp.substring(leftIndex, rightIndex);

        logger.info("ReSubmiteFlag: " + ReSubmiteFlag);
        logger.info("开始填报");

        properties.put("Referer", Referer2);
        Map<String, String> params = new HashMap<>();
        Date date = new Date();
        int hour = date.getHours();
        int min = date.getMinutes();
        int sec = date.getSeconds();
        logger.info("填报时间： hour: " + hour + " min:" + min);

        params.put("TimeNowHour", String.valueOf(hour));
        params.put("TimeNowMinute", String.valueOf(min));
        params.put("Temper1", "36");
        params.put("Temper2", String.valueOf(sec % 10));
        params.put("ReSubmiteFlag", ReSubmiteFlag);
        logger.info("填报温度： 36°" + sec % 10);

        resp = HttpUtil.post(temperatureURL, params, properties);

        if(resp.contains("填报成功")){
            logger.info("填报成功");
            System.out.println("成功");
        }else{
            logger.warning("填报失败");
            logger.warning(resp);
        }
    }

    private static void sendEmail(){

    }
}
