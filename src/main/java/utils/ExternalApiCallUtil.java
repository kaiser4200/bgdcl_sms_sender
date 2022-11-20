package utils;

import com.iict.sms.enums.ExternalApiResponseEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public final class ExternalApiCallUtil {
    private ExternalApiCallUtil(){}
    public static void disableSSL() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> sendSMS(String msisdn, String sms_string) {
        Map<String ,Object> map = new HashMap<>();
        String url = "https://bulksms.teletalk.com.bd/link_sms_send.php?op=SMS&user=BGDCLCUM&pass=BgDclcu@R87&mobile=" + msisdn + "&charset=ASCII&sms=" + sms_string;
        String responseString = null;
        try {
            disableSSL();
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            Boolean isSent = response.getStatusCode().is2xxSuccessful() && response.getBody().contains("<reply>SUCCESS");
            responseString = response.getBody();
            map.put(ExternalApiResponseEnum.URL.name(), url);
            map.put(ExternalApiResponseEnum.RESPONSE.name(), responseString);
            map.put(ExternalApiResponseEnum.IS_SENT.name(), isSent);
            return map;
        }catch (Exception e){
            map.put(ExternalApiResponseEnum.URL.name(), url);
            map.put(ExternalApiResponseEnum.RESPONSE.name(), responseString);
            map.put(ExternalApiResponseEnum.IS_SENT.name(), false);
            map.put(ExternalApiResponseEnum.BGDCL_LOG.name(), e.getMessage());
            return map;
        }
    }
}
