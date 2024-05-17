package dev.tserato.geoloc.extentions;

import com.google.gson.Gson;
import dev.tserato.geoloc.GeoLocSpigot;
import dev.tserato.geoloc.extentions.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LogToDiscord implements Listener {

    private static boolean useDiscord;
    private static String discordWebhookUrl;
    private static final Map<String, String> countryCodes = new HashMap<>();

    private final FileConfiguration config;

    public LogToDiscord(FileConfiguration config) {
        this.config = config;
        useDiscord = config.getBoolean("use-discord", false);
        discordWebhookUrl = config.getString("webhook-url", "");

        initCountryCodes();
    }

    public static boolean isUseDiscord() {
        return useDiscord;
    }

    public static String getDiscordWebhookUrl() {
        return discordWebhookUrl;
    }

    private void initCountryCodes() {
        countryCodes.put("Afghanistan", "AF");
        countryCodes.put("Albania", "AL");
        countryCodes.put("Algeria", "DZ");
        countryCodes.put("Andorra", "AD");
        countryCodes.put("Angola", "AO");
        countryCodes.put("Antigua and Barbuda", "AG");
        countryCodes.put("Argentina", "AR");
        countryCodes.put("Armenia", "AM");
        countryCodes.put("Australia", "AU");
        countryCodes.put("Austria", "AT");
        countryCodes.put("Azerbaijan", "AZ");
        countryCodes.put("Bahamas", "BS");
        countryCodes.put("Bahrain", "BH");
        countryCodes.put("Bangladesh", "BD");
        countryCodes.put("Barbados", "BB");
        countryCodes.put("Belarus", "BY");
        countryCodes.put("Belgium", "BE");
        countryCodes.put("Belize", "BZ");
        countryCodes.put("Benin", "BJ");
        countryCodes.put("Bhutan", "BT");
        countryCodes.put("Bolivia", "BO");
        countryCodes.put("Bosnia and Herzegovina", "BA");
        countryCodes.put("Botswana", "BW");
        countryCodes.put("Brazil", "BR");
        countryCodes.put("Brunei Darussalam", "BN");
        countryCodes.put("Bulgaria", "BG");
        countryCodes.put("Burkina Faso", "BF");
        countryCodes.put("Burundi", "BI");
        countryCodes.put("Cabo Verde", "CV");
        countryCodes.put("Cambodia", "KH");
        countryCodes.put("Cameroon", "CM");
        countryCodes.put("Canada", "CA");
        countryCodes.put("Central African Republic", "CF");
        countryCodes.put("Chad", "TD");
        countryCodes.put("Chile", "CL");
        countryCodes.put("China", "CN");
        countryCodes.put("Colombia", "CO");
        countryCodes.put("Comoros", "KM");
        countryCodes.put("Congo", "CG");
        countryCodes.put("Costa Rica", "CR");
        countryCodes.put("Croatia", "HR");
        countryCodes.put("Cuba", "CU");
        countryCodes.put("Cyprus", "CY");
        countryCodes.put("Czech Republic", "CZ");
        countryCodes.put("Denmark", "DK");
        countryCodes.put("Djibouti", "DJ");
        countryCodes.put("Dominica", "DM");
        countryCodes.put("Dominican Republic", "DO");
        countryCodes.put("Ecuador", "EC");
        countryCodes.put("Egypt", "EG");
        countryCodes.put("El Salvador", "SV");
        countryCodes.put("Equatorial Guinea", "GQ");
        countryCodes.put("Eritrea", "ER");
        countryCodes.put("Estonia", "EE");
        countryCodes.put("Eswatini", "SZ");
        countryCodes.put("Ethiopia", "ET");
        countryCodes.put("Fiji", "FJ");
        countryCodes.put("Finland", "FI");
        countryCodes.put("France", "FR");
        countryCodes.put("Gabon", "GA");
        countryCodes.put("Gambia", "GM");
        countryCodes.put("Georgia", "GE");
        countryCodes.put("Germany", "DE");
        countryCodes.put("Ghana", "GH");
        countryCodes.put("Greece", "GR");
        countryCodes.put("Grenada", "GD");
        countryCodes.put("Guatemala", "GT");
        countryCodes.put("Guinea", "GN");
        countryCodes.put("Guinea-Bissau", "GW");
        countryCodes.put("Guyana", "GY");
        countryCodes.put("Haiti", "HT");
        countryCodes.put("Honduras", "HN");
        countryCodes.put("Hungary", "HU");
        countryCodes.put("Iceland", "IS");
        countryCodes.put("India", "IN");
        countryCodes.put("Indonesia", "ID");
        countryCodes.put("Iran", "IR");
        countryCodes.put("Iraq", "IQ");
        countryCodes.put("Ireland", "IE");
        countryCodes.put("Israel", "IL");
        countryCodes.put("Italy", "IT");
        countryCodes.put("Jamaica", "JM");
        countryCodes.put("Japan", "JP");
        countryCodes.put("Jordan", "JO");
        countryCodes.put("Kazakhstan", "KZ");
        countryCodes.put("Kenya", "KE");
        countryCodes.put("Kiribati", "KI");
        countryCodes.put("Korea (North)", "KP");
        countryCodes.put("Korea (South)", "KR");
        countryCodes.put("Kuwait", "KW");
        countryCodes.put("Kyrgyzstan", "KG");
        countryCodes.put("Lao PDR", "LA");
        countryCodes.put("Latvia", "LV");
        countryCodes.put("Lebanon", "LB");
        countryCodes.put("Lesotho", "LS");
        countryCodes.put("Liberia", "LR");
        countryCodes.put("Libya", "LY");
        countryCodes.put("Liechtenstein", "LI");
        countryCodes.put("Lithuania", "LT");
        countryCodes.put("Luxembourg", "LU");
        countryCodes.put("Madagascar", "MG");
        countryCodes.put("Malawi", "MW");
        countryCodes.put("Malaysia", "MY");
        countryCodes.put("Maldives", "MV");
        countryCodes.put("Mali", "ML");
        countryCodes.put("Malta", "MT");
        countryCodes.put("Marshall Islands", "MH");
        countryCodes.put("Mauritania", "MR");
        countryCodes.put("Mauritius", "MU");
        countryCodes.put("Mexico", "MX");
        countryCodes.put("Micronesia", "FM");
        countryCodes.put("Moldova", "MD");
        countryCodes.put("Monaco", "MC");
        countryCodes.put("Mongolia", "MN");
        countryCodes.put("Montenegro", "ME");
        countryCodes.put("Morocco", "MA");
        countryCodes.put("Mozambique", "MZ");
        countryCodes.put("Myanmar", "MM");
        countryCodes.put("Namibia", "NA");
        countryCodes.put("Nauru", "NR");
        countryCodes.put("Nepal", "NP");
        countryCodes.put("Netherlands", "NL");
        countryCodes.put("New Zealand", "NZ");
        countryCodes.put("Nicaragua", "NI");
        countryCodes.put("Niger", "NE");
        countryCodes.put("Nigeria", "NG");
        countryCodes.put("North Macedonia", "MK");
        countryCodes.put("Norway", "NO");
        countryCodes.put("Oman", "OM");
        countryCodes.put("Pakistan", "PK");
        countryCodes.put("Palau", "PW");
        countryCodes.put("Palestinian Territory", "PS");
        countryCodes.put("Panama", "PA");
        countryCodes.put("Papua New Guinea", "PG");
        countryCodes.put("Paraguay", "PY");
        countryCodes.put("Peru", "PE");
        countryCodes.put("Philippines", "PH");
        countryCodes.put("Poland", "PL");
        countryCodes.put("Portugal", "PT");
        countryCodes.put("Qatar", "QA");
        countryCodes.put("Romania", "RO");
        countryCodes.put("Russia", "RU");
        countryCodes.put("Rwanda", "RW");
        countryCodes.put("Saint Kitts and Nevis", "KN");
        countryCodes.put("Saint Lucia", "LC");
        countryCodes.put("Saint Vincent and the Grenadines", "VC");
        countryCodes.put("Samoa", "WS");
        countryCodes.put("San Marino", "SM");
        countryCodes.put("Sao Tome and Principe", "ST");
        countryCodes.put("Saudi Arabia", "SA");
        countryCodes.put("Senegal", "SN");
        countryCodes.put("Serbia", "RS");
        countryCodes.put("Seychelles", "SC");
        countryCodes.put("Sierra Leone", "SL");
        countryCodes.put("Singapore", "SG");
        countryCodes.put("Slovakia", "SK");
        countryCodes.put("Slovenia", "SI");
        countryCodes.put("Solomon Islands", "SB");
        countryCodes.put("Somalia", "SO");
        countryCodes.put("South Africa", "ZA");
        countryCodes.put("South Sudan", "SS");
        countryCodes.put("Spain", "ES");
        countryCodes.put("Sri Lanka", "LK");
        countryCodes.put("Sudan", "SD");
        countryCodes.put("Suriname", "SR");
        countryCodes.put("Sweden", "SE");
        countryCodes.put("Switzerland", "CH");
        countryCodes.put("Syria", "SY");
        countryCodes.put("Taiwan", "TW");
        countryCodes.put("Tajikistan", "TJ");
        countryCodes.put("Tanzania", "TZ");
        countryCodes.put("Thailand", "TH");
        countryCodes.put("Timor-Leste", "TL");
        countryCodes.put("Togo", "TG");
        countryCodes.put("Tonga", "TO");
        countryCodes.put("Trinidad and Tobago", "TT");
        countryCodes.put("Tunisia", "TN");
        countryCodes.put("Turkey", "TR");
        countryCodes.put("Turkmenistan", "TM");
        countryCodes.put("Tuvalu", "TV");
        countryCodes.put("Uganda", "UG");
        countryCodes.put("Ukraine", "UA");
        countryCodes.put("United Arab Emirates", "AE");
        countryCodes.put("Uruguay", "UY");
        countryCodes.put("Uzbekistan", "UZ");
        countryCodes.put("Vanuatu", "VU");
        countryCodes.put("Vatican City", "VA");
        countryCodes.put("Venezuela", "VE");
        countryCodes.put("Vietnam", "VN");
        countryCodes.put("Yemen", "YE");
        countryCodes.put("Zambia", "ZM");
        countryCodes.put("Zimbabwe", "ZW");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) {
            String ip = player.getAddress().getAddress().getHostAddress();

            Bukkit.getScheduler().runTaskAsynchronously(GeoLocSpigot.getInstance(), () -> {
                String geoLocation = getGeoLocation(ip);
                String flagUrl = getFlagUrl(geoLocation);

                Bukkit.getScheduler().runTask(GeoLocSpigot.getInstance(), () -> logToDiscord(player.getName(), geoLocation, flagUrl));
            });
        } else if (!player.hasPlayedBefore()) {
            String ip = player.getAddress().getAddress().getHostAddress();

            Bukkit.getScheduler().runTaskAsynchronously(GeoLocSpigot.getInstance(), () -> {
                String geoLocation = getGeoLocation(ip);
                String flagUrl = getFlagUrl(geoLocation);

                Bukkit.getScheduler().runTask(GeoLocSpigot.getInstance(), () -> logToDiscordNew(player.getName(), geoLocation, flagUrl));
            });
        }

    }

    private static String getGeoLocation(String ipAddress) {
        try {
            String urlString = "http://ip-api.com/json/" + ipAddress;
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    GeoLocation geoLocation = new Gson().fromJson(in.readLine(), GeoLocation.class);
                    return geoLocation.getCountry() + ", " + geoLocation.getRegion() + ", " + geoLocation.getCity();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private static String getFlagUrl(String geoLocation) {
        String country = extractCountry(geoLocation);
        String countryCode = countryCodes.getOrDefault(country, "");
        if (!countryCode.isEmpty()) {
            return "https://flagsapi.com/" + countryCode + "/flat/64.png";
        } else {
            return "https://flagsapi.com/unknown/flat/64.png";
        }
    }

    private static String extractCountry(String geoLocation) {
        String[] parts = geoLocation.split(",");
        return parts[0].trim();
    }

    private static void logToDiscord(String playerName, String geoLocation, String flagUrl) {
        try {
            if (useDiscord && !discordWebhookUrl.isEmpty()) {
                String country = extractCountry(geoLocation);
                DiscordWebhook webhook = new DiscordWebhook(discordWebhookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject().setTitle("Player Joined").setDescription("Player " + playerName + " joined from: " + country + "!").setColor(Color.GREEN).setImage(flagUrl));
                webhook.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logToDiscordNew(String playerName, String geoLocation, String flagUrl) {
        try {
            if (useDiscord && !discordWebhookUrl.isEmpty()) {
                String country = extractCountry(geoLocation);
                DiscordWebhook webhook = new DiscordWebhook(discordWebhookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject().setTitle("New Player Joined").setDescription("New Player " + playerName + " joined from: " + country + "!").setColor(Color.YELLOW).setImage(flagUrl));
                webhook.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class GeoLocation {
        private String country;
        private String region;
        private String city;

        public String getCountry() {
            return country;
        }

        public String getRegion() {
            return region;
        }

        public String getCity() {
            return city;
        }
    }
}
