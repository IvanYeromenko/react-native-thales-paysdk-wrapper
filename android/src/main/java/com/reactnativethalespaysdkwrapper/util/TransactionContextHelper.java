package com.reactnativethalespaysdkwrapper.util;


import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardState;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardStatus;
import com.gemalto.mfs.mwsdk.payment.engine.CardScheme;
import com.gemalto.mfs.mwsdk.payment.engine.TransactionContext;
import com.payneteasy.tlv.HexUtil;


import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TransactionContextHelper {

    private static final String TAG = TransactionContextHelper.class.getSimpleName();

    // It is recommended to use SparseArray<String> instead of Map<Integer, String>
    // to optimize the performance.
    // But such implementation causes troubles when running unit tests as
    // SparseArray<> is part of android.util package
    // Thus we stick here with java.util.Map<>

//    private static SparseArray<String> currencies;
    private static Map<Integer, String> currencies;

    static{
        AppLogger.d(TAG, "static initializer started");

//        currencies = new SparseArray<>();
        currencies = new HashMap<>();

        // Note: This array was populated based on Currency.getAvailableCurrencies()
        //       which is supported on API 24 and above.

//        currencies.put(0, "XFO"); // French Gold Franc
//        currencies.put(0, "XFU"); // French UIC-Franc
        //currencies.put(4, "AFA"); // Afghan Afghani (1927-2002) - OBSOLETE
        currencies.put(8, "ALL"); // Albanian Lek
        currencies.put(12, "DZD"); // Algerian Dinar
        currencies.put(20, "ADP"); // Andorran Peseta
        //currencies.put(31, "AZM"); // Azerbaijani Manat (1993-2006) - OBSOLETE
        currencies.put(32, "ARS"); // Argentine Peso
        currencies.put(36, "AUD"); // Australian Dollar
        currencies.put(40, "ATS"); // Austrian Schilling
        currencies.put(44, "BSD"); // Bahamian Dollar
        currencies.put(48, "BHD"); // Bahraini Dinar
        currencies.put(50, "BDT"); // Bangladeshi Taka
        currencies.put(51, "AMD"); // Armenian Dram
        currencies.put(52, "BBD"); // Barbadian Dollar
        currencies.put(56, "BEF"); // Belgian Franc
        currencies.put(60, "BMD"); // Bermudan Dollar
        currencies.put(64, "BTN"); // Bhutanese Ngultrum
        currencies.put(68, "BOB"); // Bolivian Boliviano
        currencies.put(72, "BWP"); // Botswanan Pula
        currencies.put(84, "BZD"); // Belize Dollar
        currencies.put(90, "SBD"); // Solomon Islands Dollar
        currencies.put(96, "BND"); // Brunei Dollar
        currencies.put(100, "BGL"); // Bulgarian Hard Lev
        currencies.put(104, "MMK"); // Myanma Kyat
        currencies.put(108, "BIF"); // Burundian Franc
        //currencies.put(112, "BYB"); // Belarusian Ruble (1994-1999) - OBSOLETE
        currencies.put(116, "KHR"); // Cambodian Riel
        currencies.put(124, "CAD"); // Canadian Dollar
        currencies.put(132, "CVE"); // Cape Verdean Escudo
        currencies.put(136, "KYD"); // Cayman Islands Dollar
        currencies.put(144, "LKR"); // Sri Lankan Rupee
        currencies.put(152, "CLP"); // Chilean Peso
        currencies.put(156, "CNY"); // Chinese Yuan
        currencies.put(170, "COP"); // Colombian Peso
        currencies.put(174, "KMF"); // Comorian Franc
        currencies.put(188, "CRC"); // Costa Rican Colón
        currencies.put(191, "HRK"); // Kuna
        currencies.put(192, "CUP"); // Cuban Peso
        currencies.put(196, "CYP"); // Cypriot Pound
        currencies.put(203, "CZK"); // Czech Republic Koruna
        currencies.put(208, "DKK"); // Danish Krone
        currencies.put(214, "DOP"); // Dominican Peso
        currencies.put(222, "SVC"); // Salvadoran Colón
        currencies.put(230, "ETB"); // Ethiopian Birr
        currencies.put(232, "ERN"); // Eritrean Nakfa
        currencies.put(233, "EEK"); // Estonian Kroon
        currencies.put(238, "FKP"); // Falkland Islands Pound
        currencies.put(242, "FJD"); // Fijian Dollar
        currencies.put(246, "FIM"); // Finnish Markka
        currencies.put(250, "FRF"); // French Franc
        currencies.put(262, "DJF"); // Djiboutian Franc
        currencies.put(270, "GMD"); // Gambian Dalasi
        currencies.put(276, "DEM"); // German Mark
        //currencies.put(288, "GHC"); // Ghanaian Cedi (1979-2007) - OBSOLETE
        currencies.put(292, "GIP"); // Gibraltar Pound
        currencies.put(300, "GRD"); // Greek Drachma
        currencies.put(320, "GTQ"); // Guatemalan Quetzal
        currencies.put(324, "GNF"); // Guinean Franc
        currencies.put(328, "GYD"); // Guyanaese Dollar
        currencies.put(332, "HTG"); // Haitian Gourde
        currencies.put(340, "HNL"); // Honduran Lempira
        currencies.put(344, "HKD"); // Hong Kong Dollar
        currencies.put(348, "HUF"); // Hungarian Forint
        currencies.put(352, "ISK"); // Icelandic Króna
        currencies.put(356, "INR"); // Indian Rupee
        currencies.put(360, "IDR"); // Indonesian Rupiah
        currencies.put(364, "IRR"); // Iranian Rial
        currencies.put(368, "IQD"); // Iraqi Dinar
        currencies.put(372, "IEP"); // Irish Pound
        currencies.put(376, "ILS"); // Israeli New Sheqel
        currencies.put(380, "ITL"); // Italian Lira
        currencies.put(388, "JMD"); // Jamaican Dollar
        currencies.put(392, "JPY"); // Japanese Yen
        currencies.put(398, "KZT"); // Kazakhstani Tenge
        currencies.put(400, "JOD"); // Jordanian Dinar
        currencies.put(404, "KES"); // Kenyan Shilling
        currencies.put(408, "KPW"); // North Korean Won
        currencies.put(410, "KRW"); // South Korean Won
        currencies.put(414, "KWD"); // Kuwaiti Dinar
        currencies.put(417, "KGS"); // Kyrgystani Som
        currencies.put(418, "LAK"); // Laotian Kip
        currencies.put(422, "LBP"); // Lebanese Pound
        currencies.put(426, "LSL"); // Lesotho Loti
        currencies.put(428, "LVL"); // Latvian Lats
        currencies.put(430, "LRD"); // Liberian Dollar
        currencies.put(434, "LYD"); // Libyan Dinar
        currencies.put(440, "LTL"); // Lithuanian Litas
        currencies.put(442, "LUF"); // Luxembourgian Franc
        currencies.put(446, "MOP"); // Macanese Pataca
        currencies.put(450, "MGF"); // Malagasy Franc
        currencies.put(454, "MWK"); // Malawian Malawi Kwacha
        currencies.put(458, "MYR"); // Malaysian Ringgit
        currencies.put(462, "MVR"); // Maldivian Rufiyaa
        currencies.put(470, "MTL"); // Maltese Lira
        currencies.put(478, "MRO"); // Mauritanian Ouguiya
        currencies.put(480, "MUR"); // Mauritian Rupee
        currencies.put(484, "MXN"); // Mexican Peso
        currencies.put(496, "MNT"); // Mongolian Tugrik
        currencies.put(498, "MDL"); // Moldovan Leu
        currencies.put(504, "MAD"); // Moroccan Dirham
        //currencies.put(508, "MZM"); // Mozambican Metical (1980-2006) - OBSOLETE
        currencies.put(512, "OMR"); // Omani Rial
        currencies.put(516, "NAD"); // Namibian Dollar
        currencies.put(524, "NPR"); // Nepalese Rupee
        currencies.put(528, "NLG"); // Dutch Guilder
        currencies.put(532, "ANG"); // Netherlands Antillean Guilder
        currencies.put(533, "AWG"); // Aruban Florin
        currencies.put(548, "VUV"); // Vanuatu Vatu
        currencies.put(554, "NZD"); // New Zealand Dollar
        currencies.put(558, "NIO"); // Nicaraguan Córdoba
        currencies.put(566, "NGN"); // Nigerian Naira
        currencies.put(578, "NOK"); // Norwegian Krone
        currencies.put(586, "PKR"); // Pakistani Rupee
        currencies.put(590, "PAB"); // Panamanian Balboa
        currencies.put(598, "PGK"); // Papua New Guinean Kina
        currencies.put(600, "PYG"); // Paraguayan Guarani
        currencies.put(604, "PEN"); // Peruvian Sol
        currencies.put(608, "PHP"); // Philippine Peso
        currencies.put(620, "PTE"); // Portuguese Escudo
        currencies.put(624, "GWP"); // Guinea-Bissau Peso
        currencies.put(626, "TPE"); // Timorese Escudo
        currencies.put(634, "QAR"); // Qatari Rial
        currencies.put(643, "RUB"); // Russian Ruble
        currencies.put(646, "RWF"); // Rwandan Franc
        currencies.put(654, "SHP"); // Saint Helena Pound
        currencies.put(678, "STD"); // São Tomé and Príncipe Dobra
        currencies.put(682, "SAR"); // Saudi Riyal
        currencies.put(690, "SCR"); // Seychellois Rupee
        currencies.put(694, "SLL"); // Sierra Leonean Leone
        currencies.put(702, "SGD"); // Singapore Dollar
        currencies.put(703, "SKK"); // Slovak Koruna
        currencies.put(704, "VND"); // Vietnamese Dong
        currencies.put(705, "SIT"); // Slovenian Tolar
        currencies.put(706, "SOS"); // Somali Shilling
        currencies.put(710, "ZAR"); // South African Rand
        //currencies.put(716, "ZWD"); // Zimbabwean Dollar (1980-2008) - OBSOLETE
        currencies.put(724, "ESP"); // Spanish Peseta
        currencies.put(728, "SSP"); // South Sudanese Pound
        //currencies.put(736, "SDD"); // Sudanese Dinar (1992-2007) - OBSOLETE
        currencies.put(740, "SRG"); // Surinamese Guilder
        currencies.put(748, "SZL"); // Swazi Lilangeni
        currencies.put(752, "SEK"); // Swedish Krona
        currencies.put(756, "CHF"); // Swiss Franc
        currencies.put(760, "SYP"); // Syrian Pound
        currencies.put(764, "THB"); // Thai Baht
        currencies.put(776, "TOP"); // Tongan Paʻanga
        currencies.put(780, "TTD"); // Trinidad and Tobago Dollar
        currencies.put(784, "AED"); // United Arab Emirates Dirham
        currencies.put(788, "TND"); // Tunisian Dinar
        //currencies.put(792, "TRL"); // Turkish Lira (1922-2005) - OBSOLETE
        //currencies.put(795, "TMM"); // Turkmenistani Manat (1993-2009) - OBSOLETE
        currencies.put(800, "UGX"); // Ugandan Shilling
        currencies.put(807, "MKD"); // Macedonian Denar
        //currencies.put(810, "RUR"); // Russian Ruble (1991-1998) - OBSOLETE
        currencies.put(818, "EGP"); // Egyptian Pound
        currencies.put(826, "GBP"); // British Pound Sterling
        currencies.put(834, "TZS"); // Tanzanian Shilling
        currencies.put(840, "USD"); // US Dollar
        currencies.put(858, "UYU"); // Uruguayan Peso
        currencies.put(860, "UZS"); // Uzbekistan Som
        //currencies.put(862, "VEB"); // Venezuelan Bolívar (1871-2008) - OBSOLETE
        currencies.put(882, "WST"); // Samoan Tala
        currencies.put(886, "YER"); // Yemeni Rial
        //currencies.put(891, "YUM"); // Yugoslavian New Dinar (1994-2002) - OBSOLETE
        //currencies.put(891, "CSD"); // Serbian Dinar (2002-2006) - OBSOLETE
        currencies.put(894, "ZMK"); // Zambian Kwacha
        currencies.put(901, "TWD"); // New Taiwan Dollar
        currencies.put(931, "CUC"); // Cuban Convertible Peso
        currencies.put(932, "ZWL"); // Zimbabwean Dollar (2009)
        currencies.put(933, "BYN"); // Belarusian Ruble
        currencies.put(934, "TMT"); // Turkmenistani Manat
        currencies.put(935, "ZWR"); // Zimbabwean Dollar (2008)
        currencies.put(936, "GHS"); // Ghanaian Cedi
        currencies.put(937, "VEF"); // Venezuelan Bolívar
        currencies.put(938, "SDG"); // Sudanese Pound
        currencies.put(940, "UYI"); // UYI
        currencies.put(941, "RSD"); // Serbian Dinar
        currencies.put(942, "ZWN"); // ZWN
        currencies.put(943, "MZN"); // Mozambican Metical
        currencies.put(944, "AZN"); // Azerbaijani Manat
        currencies.put(945, "AYM"); // AYM
        currencies.put(946, "RON"); // Romanian Leu
        //currencies.put(946, "ROL"); // Romanian Leu (1952-2006) - OBSOLETE
        currencies.put(947, "CHE"); // CHE
        currencies.put(948, "CHW"); // CHW
        currencies.put(949, "TRY"); // Turkish Lira
        currencies.put(950, "XAF"); // CFA Franc BEAC
        currencies.put(951, "XCD"); // East Caribbean Dollar
        currencies.put(952, "XOF"); // CFA Franc BCEAO
        currencies.put(953, "XPF"); // CFP Franc
        currencies.put(955, "XBA"); // European Composite Unit
        currencies.put(956, "XBB"); // European Monetary Unit
        currencies.put(957, "XBC"); // European Unit of Account (XBC)
        currencies.put(958, "XBD"); // European Unit of Account (XBD)
        currencies.put(959, "XAU"); // Gold
        currencies.put(960, "XDR"); // Special Drawing Rights
        currencies.put(961, "XAG"); // Silver
        currencies.put(962, "XPT"); // Platinum
        currencies.put(963, "XTS"); // Testing Currency Code
        currencies.put(964, "XPD"); // Palladium
        currencies.put(965, "XUA"); // ADB Unit of Account
        currencies.put(967, "ZMW"); // ZMW
        currencies.put(968, "SRD"); // Surinamese Dollar
        currencies.put(969, "MGA"); // Malagasy Ariary
        currencies.put(970, "COU"); // COU
        currencies.put(971, "AFN"); // Afghan Afghani
        currencies.put(972, "TJS"); // Tajikistani Somoni
        currencies.put(973, "AOA"); // Angolan Kwanza
        //currencies.put(974, "BYR"); // Belarusian Ruble (2000-2016) - OBSOLETE
        currencies.put(975, "BGN"); // Bulgarian Lev
        currencies.put(976, "CDF"); // Congolese Franc
        currencies.put(977, "BAM"); // Bosnia-Herzegovina Convertible Mark
        currencies.put(978, "EUR"); // Euro
        currencies.put(979, "MXV"); // Mexican Investment Unit
        currencies.put(980, "UAH"); // Ukrainian Hryvnia
        currencies.put(981, "GEL"); // Georgian Lari
        currencies.put(984, "BOV"); // Bolivian Mvdol
        currencies.put(985, "PLN"); // Polish Zloty
        currencies.put(986, "BRL"); // Brazilian Real
        currencies.put(990, "CLF"); // Chilean Unit of Account (UF)
        currencies.put(994, "XSU"); // Sucre
        currencies.put(997, "USN"); // US Dollar (Next day)
        currencies.put(998, "USS"); // US Dollar (Same day)
        currencies.put(999, "XXX"); // Unknown Currency

        AppLogger.d(TAG, "currencies.size()=" + currencies.size());

        AppLogger.d(TAG, "static initializer ended");
    }



    /**
     * Provides mocked TransactionContext for testing purpose
     * @return
     */
    public static TransactionContext getMockTransactionContext(){
        return  new TransactionContext() {
            @Override
            public String getAid() {
                return "A0000000031010";
            }

            @Override
            public byte[] getRawAmount() {
                return new byte[]{0x00, 0x00, 0x00, 0x00, 0x12, 0x34};
            }

            @Override
            public double getAmount() {
                return 12.34;
            }

            @Override
            public byte[] getCurrencyCode() {
                return new byte[]{0x09, 0x78};
            }

            @Override
            public byte[] getTrxDate() {
                return new byte[0];
            }

            @Override
            public byte getTrxType() {
                return 0;
            }

            @Override
            public DigitalizedCardStatus getDigitalizedCardStatus() {
                return new DigitalizedCardStatus() {
                    @Override
                    public DigitalizedCardState getState() {
                        return DigitalizedCardState.ACTIVE;
                    }

                    @Override
                    public int getNumberOfPaymentsLeft() {
                        return 1;
                    }

                    @Override
                    public boolean needsReplenishment() {
                        return false;
                    }

                    @Override
                    public String getExpiryDate() {
                        return null;
                    }
                };
            }

            @Override
            public void wipe() {
                // nothing to be done for mocked data
            }

            @Override
            public CardScheme getScheme() {
                return null;
            }

        };
    }


    /**
     * Resolves currency from the currency code provided as byte[]
     * @param currencyNumericCode
     * @return Currency instance associated with the given currency code or unknown currency (XXX)
     * if the lookup fails
     */
    public static Currency getCurrency(byte[] currencyNumericCode){
        if(currencyNumericCode == null) throw new IllegalArgumentException("currencyNumericCode cannot be null");

        final String strCurrencyNumericCode = HexUtil.toHexString(currencyNumericCode);

        return getCurrency(strCurrencyNumericCode);

    }

    /**
     * Implementation of Currency retrieval which uses a local look-up map
     * instead of {@link Currency#getAvailableCurrencies()} which could be used only on API 24 and above.
     * @param currencyNumericCode
     * @return
     */
    private static Currency getCurrency(final String currencyNumericCode){

        AppLogger.d(TAG, "getCurrency(): currencyNumericCode=" + currencyNumericCode);

        // ISO 4217 alphabetic code for Unknown currency
        final Currency unknownCurrency = Currency.getInstance("XXX");

        int numericCode;

        try {
            numericCode = Integer.parseInt(currencyNumericCode);
            AppLogger.d(TAG, "numericCode=" + numericCode);
        }
        catch (NumberFormatException nfe){
            AppLogger.e(TAG,"Unable to parse numeric code. Reason" + nfe.getMessage());
            return unknownCurrency;
        }

        final String currencyCode = currencies.get(numericCode);
        AppLogger.d(TAG, "currencyCode=" + currencyCode);

        if(currencyCode == null){
            return unknownCurrency;
        }
        else{
            return Currency.getInstance(currencyCode);
        }
    }

    public static String formatAmountWithCurrency(final TransactionContext transactionContext){
        return formatAmountWithCurrency(transactionContext, Locale.getDefault());
    }

    /**
     * Formats amount with respect to locale and transaction currency
     * @param transactionContext
     * @return Formated string
     */
    public static String formatAmountWithCurrency(final TransactionContext transactionContext,
                                                  final Locale locale){

        if(transactionContext == null) return null;

        AppLogger.d(TAG, "locale=" + locale);
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);

        final Currency currency = getCurrency(transactionContext.getCurrencyCode());
        final int fractionDigits = currency.getDefaultFractionDigits();
        AppLogger.d(TAG, "Currency fraction digits: " + fractionDigits);

        numberFormat.setCurrency(currency);
        numberFormat.setMinimumFractionDigits(fractionDigits);
        numberFormat.setMaximumFractionDigits(fractionDigits);

        final String formattedAmount = numberFormat.format(transactionContext.getAmount());
        AppLogger.d(TAG, "Formatted amount: " + formattedAmount);

        return formattedAmount;
    }
}
