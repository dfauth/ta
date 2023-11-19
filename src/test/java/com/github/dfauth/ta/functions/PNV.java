package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Slf4j
public class PNV {

    public static final BigDecimal YAHOO_RSI = new BigDecimal("34.3385");

    public static final BigDecimal WESTPAC_RSI = new BigDecimal("34.36");

    public static final String PRICE_STRING = "2022-05-20,1.300000,1.365000,1.260000,1.325000,1.325000,4830016\n" +
            "2022-05-23,1.345000,1.350000,1.225000,1.225000,1.225000,3533030\n" +
            "2022-05-24,1.255000,1.265000,1.185000,1.185000,1.185000,1836328\n" +
            "2022-05-25,1.190000,1.205000,1.150000,1.175000,1.175000,2175431\n" +
            "2022-05-26,1.185000,1.240000,1.185000,1.185000,1.185000,2837744\n" +
            "2022-05-27,1.220000,1.265000,1.210000,1.225000,1.225000,1998258\n" +
            "2022-05-30,1.245000,1.320000,1.240000,1.305000,1.305000,2839799\n" +
            "2022-05-31,1.310000,1.330000,1.230000,1.235000,1.235000,2896960\n" +
            "2022-06-01,1.250000,1.260000,1.180000,1.195000,1.195000,3041300\n" +
            "2022-06-02,1.180000,1.195000,1.110000,1.125000,1.125000,3028947\n" +
            "2022-06-03,1.155000,1.200000,1.145000,1.185000,1.185000,2541108\n" +
            "2022-06-06,1.150000,1.175000,1.125000,1.135000,1.135000,2166836\n" +
            "2022-06-07,1.130000,1.180000,1.110000,1.115000,1.115000,2107408\n" +
            "2022-06-08,1.125000,1.180000,1.120000,1.160000,1.160000,2640544\n" +
            "2022-06-09,1.165000,1.210000,1.145000,1.160000,1.160000,1787670\n" +
            "2022-06-10,1.180000,1.195000,1.150000,1.155000,1.155000,2074965\n" +
            "2022-06-14,1.115000,1.245000,1.115000,1.245000,1.245000,4175717\n" +
            "2022-06-15,1.250000,1.315000,1.230000,1.305000,1.305000,4098926\n" +
            "2022-06-16,1.340000,1.360000,1.290000,1.290000,1.290000,4261536\n" +
            "2022-06-17,1.250000,1.352500,1.205000,1.335000,1.335000,19835247\n" +
            "2022-06-20,1.360000,1.445000,1.300000,1.310000,1.310000,2982789\n" +
            "2022-06-21,1.335000,1.340000,1.270000,1.275000,1.275000,1002107\n" +
            "2022-06-22,1.295000,1.300000,1.205000,1.215000,1.215000,1638473\n" +
            "2022-06-23,1.210000,1.245000,1.150000,1.160000,1.160000,1701411\n" +
            "2022-06-24,1.170000,1.257500,1.165000,1.250000,1.250000,2704063\n" +
            "2022-06-27,1.255000,1.320000,1.230000,1.310000,1.310000,1954788\n" +
            "2022-06-28,1.315000,1.415000,1.295000,1.405000,1.405000,3027551\n" +
            "2022-06-29,1.360000,1.405000,1.300000,1.400000,1.400000,1946656\n" +
            "2022-06-30,1.395000,1.470000,1.330000,1.355000,1.355000,3591772\n" +
            "2022-07-01,1.355000,1.422500,1.305000,1.305000,1.305000,2256250\n" +
            "2022-07-04,1.340000,1.375000,1.320000,1.335000,1.335000,1583452\n" +
            "2022-07-05,1.320000,1.475000,1.315000,1.470000,1.470000,2798954\n" +
            "2022-07-06,1.475000,1.545000,1.465000,1.485000,1.485000,3233368\n" +
            "2022-07-07,1.500000,1.540000,1.465000,1.535000,1.535000,3798267\n" +
            "2022-07-08,1.550000,1.600000,1.540000,1.560000,1.560000,4358675\n" +
            "2022-07-11,1.580000,1.590000,1.490000,1.550000,1.550000,2233461\n" +
            "2022-07-12,1.540000,1.552500,1.455000,1.475000,1.475000,1188112\n" +
            "2022-07-13,1.450000,1.540000,1.450000,1.540000,1.540000,1681780\n" +
            "2022-07-14,1.535000,1.580000,1.490000,1.550000,1.550000,2190954\n" +
            "2022-07-15,1.520000,1.530000,1.450000,1.450000,1.450000,1484253\n" +
            "2022-07-18,1.475000,1.535000,1.415000,1.535000,1.535000,1719534\n" +
            "2022-07-19,1.535000,1.535000,1.445000,1.485000,1.485000,2033240\n" +
            "2022-07-20,1.520000,1.580000,1.520000,1.575000,1.575000,3364793\n" +
            "2022-07-21,1.580000,1.630000,1.560000,1.580000,1.580000,2889890\n" +
            "2022-07-22,1.600000,1.725000,1.560000,1.725000,1.725000,4393009\n" +
            "2022-07-25,1.685000,1.700000,1.480000,1.490000,1.490000,4409315\n" +
            "2022-07-26,1.480000,1.487500,1.360000,1.460000,1.460000,3470244\n" +
            "2022-07-27,1.410000,1.450000,1.390000,1.420000,1.420000,2527081\n" +
            "2022-07-28,1.460000,1.512500,1.440000,1.510000,1.510000,2443154\n" +
            "2022-07-29,1.520000,1.640000,1.510000,1.640000,1.640000,3157111\n" +
            "2022-08-01,1.665000,1.710000,1.595000,1.600000,1.600000,2414531\n" +
            "2022-08-02,1.585000,1.600000,1.550000,1.580000,1.580000,1179923\n" +
            "2022-08-03,1.600000,1.710000,1.552500,1.710000,1.710000,2732225\n" +
            "2022-08-04,1.730000,1.840000,1.720000,1.810000,1.810000,3814891\n" +
            "2022-08-05,1.810000,1.920000,1.785000,1.910000,1.910000,3089444\n" +
            "2022-08-08,1.970000,2.045000,1.920000,2.020000,2.020000,3936654\n" +
            "2022-08-09,2.010000,2.200000,2.010000,2.120000,2.120000,3736801\n" +
            "2022-08-10,2.110000,2.140000,2.040000,2.120000,2.120000,4287886\n" +
            "2022-08-11,2.170000,2.250000,2.120000,2.180000,2.180000,2302685\n" +
            "2022-08-12,2.150000,2.220000,2.050000,2.100000,2.100000,2419869\n" +
            "2022-08-15,2.160000,2.210000,2.120000,2.140000,2.140000,1711266\n" +
            "2022-08-16,2.140000,2.200000,2.100000,2.100000,2.100000,1650058\n" +
            "2022-08-17,2.080000,2.220000,2.040000,2.180000,2.180000,2336355\n" +
            "2022-08-18,2.160000,2.180000,2.070000,2.120000,2.120000,1276859\n" +
            "2022-08-19,2.120000,2.120000,1.980000,1.985000,1.985000,1927796\n" +
            "2022-08-22,1.960000,2.060000,1.915000,2.030000,2.030000,1502375\n" +
            "2022-08-23,1.980000,2.090000,1.965000,1.965000,1.965000,1246548\n" +
            "2022-08-24,1.965000,2.010000,1.950000,2.000000,2.000000,851567\n" +
            "2022-08-25,2.000000,2.080000,1.990000,2.020000,2.020000,1172366\n" +
            "2022-08-26,2.040000,2.090000,1.635000,1.640000,1.640000,5482672\n" +
            "2022-08-29,1.535000,1.540000,1.340000,1.360000,1.360000,8158999\n" +
            "2022-08-30,1.390000,1.420000,1.290000,1.335000,1.335000,4293226\n" +
            "2022-08-31,1.360000,1.360000,1.297500,1.310000,1.310000,2662834\n" +
            "2022-09-01,1.300000,1.340000,1.275000,1.335000,1.335000,2330986\n" +
            "2022-09-02,1.320000,1.340000,1.270000,1.270000,1.270000,1453598\n" +
            "2022-09-05,1.235000,1.365000,1.225000,1.320000,1.320000,2323179\n" +
            "2022-09-06,1.330000,1.355000,1.280000,1.300000,1.300000,1191855\n" +
            "2022-09-07,1.315000,1.365000,1.305000,1.310000,1.310000,897729\n" +
            "2022-09-08,1.350000,1.382500,1.340000,1.375000,1.375000,1386376\n" +
            "2022-09-09,1.390000,1.440000,1.370000,1.435000,1.435000,1449449\n" +
            "2022-09-12,1.445000,1.470000,1.395000,1.410000,1.410000,1541334\n" +
            "2022-09-13,1.400000,1.410000,1.360000,1.410000,1.410000,875291\n" +
            "2022-09-14,1.360000,1.445000,1.350000,1.415000,1.415000,1798999\n" +
            "2022-09-15,1.400000,1.425000,1.350000,1.355000,1.355000,827073\n" +
            "2022-09-16,1.375000,1.380000,1.335000,1.355000,1.355000,340638\n" +
            "2022-09-19,1.500000,1.635000,1.450000,1.490000,1.490000,4799904\n" +
            "2022-09-20,1.520000,1.590000,1.395000,1.420000,1.420000,2493866\n" +
            "2022-09-21,1.425000,1.485000,1.405000,1.470000,1.470000,1783367\n" +
            "2022-09-23,1.475000,1.490000,1.375000,1.400000,1.400000,2308985\n" +
            "2022-09-26,1.360000,1.440000,1.360000,1.395000,1.395000,1548686\n" +
            "2022-09-27,1.370000,1.430000,1.370000,1.430000,1.430000,1082693\n" +
            "2022-09-28,1.450000,1.460000,1.390000,1.455000,1.455000,2197768\n" +
            "2022-09-29,1.500000,1.520000,1.390000,1.395000,1.395000,2149159\n" +
            "2022-09-30,1.370000,1.387500,1.285000,1.300000,1.300000,1981287\n" +
            "2022-10-03,1.280000,1.385000,1.280000,1.330000,1.330000,1140596\n" +
            "2022-10-04,1.350000,1.455000,1.350000,1.445000,1.445000,2198522\n" +
            "2022-10-05,1.470000,1.470000,1.395000,1.465000,1.465000,1569500\n" +
            "2022-10-06,1.500000,1.805000,1.495000,1.805000,1.805000,5864433\n" +
            "2022-10-07,1.770000,1.835000,1.675000,1.750000,1.750000,3051164\n" +
            "2022-10-10,1.720000,1.730000,1.645000,1.660000,1.660000,1942818\n" +
            "2022-10-11,1.695000,1.780000,1.690000,1.760000,1.760000,2106207\n" +
            "2022-10-12,1.755000,1.780000,1.685000,1.690000,1.690000,2341429\n" +
            "2022-10-13,1.690000,1.775000,1.670000,1.715000,1.715000,2356887\n" +
            "2022-10-14,1.760000,1.780000,1.675000,1.720000,1.720000,2473596\n" +
            "2022-10-17,1.700000,1.815000,1.685000,1.775000,1.775000,2626214\n" +
            "2022-10-18,1.835000,2.010000,1.810000,1.995000,1.995000,3973018\n" +
            "2022-10-19,1.945000,2.010000,1.910000,1.970000,1.970000,2528454\n" +
            "2022-10-20,1.930000,1.975000,1.905000,1.935000,1.935000,1911053\n" +
            "2022-10-21,1.930000,1.960000,1.815000,1.920000,1.920000,1818515\n" +
            "2022-10-24,1.970000,2.020000,1.935000,2.010000,2.010000,2018229\n" +
            "2022-10-25,2.020000,2.100000,1.945000,1.945000,1.945000,1926348\n" +
            "2022-10-26,1.975000,1.975000,1.820000,1.875000,1.875000,2160596\n" +
            "2022-10-27,1.880000,2.010000,1.880000,1.980000,1.980000,1653038\n" +
            "2022-10-28,1.960000,2.000000,1.920000,1.955000,1.955000,838145\n" +
            "2022-10-31,1.960000,2.040000,1.940000,2.000000,2.000000,1310894\n" +
            "2022-11-01,2.010000,2.050000,1.970000,2.040000,2.040000,2125352\n" +
            "2022-11-02,2.050000,2.130000,1.995000,2.060000,2.060000,2249754\n" +
            "2022-11-03,1.980000,2.070000,1.940000,1.995000,1.995000,1286791\n" +
            "2022-11-04,1.990000,2.040000,1.920000,2.040000,2.040000,1391435\n" +
            "2022-11-07,2.050000,2.090000,2.010000,2.030000,2.030000,1074713\n" +
            "2022-11-08,2.010000,2.070000,1.995000,2.010000,2.010000,1534780\n" +
            "2022-11-09,2.000000,2.035000,1.960000,1.985000,1.985000,936896\n" +
            "2022-11-10,1.970000,1.975000,1.910000,1.910000,1.910000,1156344\n" +
            "2022-11-11,1.980000,2.000000,1.920000,1.990000,1.990000,11991563\n" +
            "2022-11-14,2.000000,2.140000,1.980000,2.120000,2.120000,2086990\n" +
            "2022-11-15,2.090000,2.140000,2.070000,2.130000,2.130000,1342067\n" +
            "2022-11-16,2.120000,2.180000,2.060000,2.180000,2.180000,973770\n" +
            "2022-11-17,2.180000,2.180000,2.120000,2.160000,2.160000,783424\n" +
            "2022-11-18,2.150000,2.160000,2.100000,2.100000,2.100000,788321\n" +
            "2022-11-21,2.120000,2.180000,2.080000,2.090000,2.090000,839142\n" +
            "2022-11-22,2.090000,2.090000,2.090000,2.090000,2.090000,0\n" +
            "2022-11-23,2.000000,2.000000,1.900000,1.905000,1.905000,3259049\n" +
            "2022-11-24,1.900000,1.950000,1.900000,1.905000,1.905000,1695922\n" +
            "2022-11-25,1.905000,1.990000,1.905000,1.955000,1.955000,1852517\n" +
            "2022-11-28,1.950000,1.970000,1.885000,1.915000,1.915000,2206831\n" +
            "2022-11-29,1.900000,2.010000,1.900000,1.940000,1.940000,1859859\n" +
            "2022-11-30,1.940000,2.020000,1.925000,2.020000,2.020000,2129189\n" +
            "2022-12-01,2.030000,2.080000,2.010000,2.030000,2.030000,1405384\n" +
            "2022-12-02,2.030000,2.050000,1.990000,1.990000,1.990000,1050866\n" +
            "2022-12-05,1.970000,2.010000,1.935000,1.935000,1.935000,1313232\n" +
            "2022-12-06,1.930000,1.990000,1.930000,1.940000,1.940000,1686527\n" +
            "2022-12-07,1.935000,1.955000,1.900000,1.900000,1.900000,1834988\n" +
            "2022-12-08,1.905000,1.950000,1.905000,1.935000,1.935000,2008042\n" +
            "2022-12-09,1.940000,1.980000,1.930000,1.945000,1.945000,1646951\n" +
            "2022-12-12,1.935000,1.985000,1.900000,1.960000,1.960000,1324555\n" +
            "2022-12-13,1.950000,2.060000,1.940000,2.040000,2.040000,2012318\n" +
            "2022-12-14,2.060000,2.100000,2.015000,2.080000,2.080000,1622712\n" +
            "2022-12-15,2.080000,2.080000,2.000000,2.000000,2.000000,1323993\n" +
            "2022-12-16,2.010000,2.090000,1.985000,2.050000,2.050000,1803772\n" +
            "2022-12-19,2.040000,2.040000,1.910000,1.910000,1.910000,1542287\n" +
            "2022-12-20,1.930000,1.930000,1.805000,1.805000,1.805000,1786533\n" +
            "2022-12-21,1.850000,1.885000,1.825000,1.855000,1.855000,1644395\n" +
            "2022-12-22,1.855000,1.920000,1.850000,1.905000,1.905000,711735\n" +
            "2022-12-23,1.890000,1.950000,1.865000,1.940000,1.940000,840206\n" +
            "2022-12-28,1.945000,1.975000,1.905000,1.975000,1.975000,755717\n" +
            "2022-12-29,1.980000,2.030000,1.950000,1.960000,1.960000,981558\n" +
            "2022-12-30,1.960000,2.040000,1.960000,2.020000,2.020000,487036\n" +
            "2023-01-03,2.040000,2.070000,1.970000,2.040000,2.040000,705788\n" +
            "2023-01-04,2.060000,2.090000,2.010000,2.020000,2.020000,1262081\n" +
            "2023-01-05,2.040000,2.170000,2.020000,2.170000,2.170000,2281785\n" +
            "2023-01-06,2.160000,2.200000,2.120000,2.160000,2.160000,1097417\n" +
            "2023-01-09,2.170000,2.300000,2.160000,2.300000,2.300000,2684705\n" +
            "2023-01-10,2.300000,2.530000,2.270000,2.500000,2.500000,4774214\n" +
            "2023-01-11,2.470000,2.480000,2.350000,2.440000,2.440000,3284802\n" +
            "2023-01-12,2.440000,2.530000,2.440000,2.530000,2.530000,2022252\n" +
            "2023-01-13,2.530000,2.580000,2.480000,2.540000,2.540000,2540893\n" +
            "2023-01-16,2.610000,2.650000,2.515000,2.590000,2.590000,3064264\n" +
            "2023-01-17,2.550000,2.560000,2.400000,2.420000,2.420000,2459004\n" +
            "2023-01-18,2.420000,2.540000,2.420000,2.540000,2.540000,1857404\n" +
            "2023-01-19,2.510000,2.575000,2.430000,2.540000,2.540000,2203853\n" +
            "2023-01-20,2.550000,2.580000,2.480000,2.530000,2.530000,1461958\n" +
            "2023-01-23,2.540000,2.550000,2.440000,2.540000,2.540000,1656481\n" +
            "2023-01-24,2.550000,2.550000,2.470000,2.510000,2.510000,1506332\n" +
            "2023-01-25,2.520000,2.520000,2.440000,2.440000,2.440000,1006036\n" +
            "2023-01-27,2.420000,2.420000,2.350000,2.390000,2.390000,1749013\n" +
            "2023-01-30,2.390000,2.560000,2.380000,2.490000,2.490000,2159052\n" +
            "2023-01-31,2.490000,2.570000,2.485000,2.510000,2.510000,1938736\n" +
            "2023-02-01,2.540000,2.550000,2.470000,2.490000,2.490000,1667016\n" +
            "2023-02-02,2.530000,2.600000,2.495000,2.590000,2.590000,2064056\n" +
            "2023-02-03,2.590000,2.600000,2.540000,2.560000,2.560000,1566801\n" +
            "2023-02-06,2.540000,2.590000,2.510000,2.560000,2.560000,1411043\n" +
            "2023-02-07,2.550000,2.710000,2.550000,2.650000,2.650000,3684759\n" +
            "2023-02-08,2.680000,2.690000,2.320000,2.330000,2.330000,3465086\n" +
            "2023-02-09,2.320000,2.400000,2.290000,2.310000,2.310000,2317636\n" +
            "2023-02-10,2.300000,2.300000,2.130000,2.170000,2.170000,2427509\n" +
            "2023-02-13,2.160000,2.305000,2.130000,2.270000,2.270000,1884236\n" +
            "2023-02-14,2.270000,2.350000,2.235000,2.270000,2.270000,1308175\n" +
            "2023-02-15,2.280000,2.315000,2.240000,2.260000,2.260000,850186\n" +
            "2023-02-16,2.280000,2.310000,2.250000,2.310000,2.310000,1673220\n" +
            "2023-02-17,2.300000,2.320000,2.250000,2.260000,2.260000,983838\n" +
            "2023-02-20,2.300000,2.330000,2.240000,2.250000,2.250000,1710954\n" +
            "2023-02-21,2.290000,2.345000,2.260000,2.310000,2.310000,1442363\n" +
            "2023-02-22,2.290000,2.290000,2.220000,2.220000,2.220000,2401530\n" +
            "2023-02-23,2.210000,2.320000,2.170000,2.280000,2.280000,1615205\n" +
            "2023-02-24,2.290000,2.430000,2.130000,2.220000,2.220000,4244524\n" +
            "2023-02-27,2.220000,2.340000,2.170000,2.320000,2.320000,1863616\n" +
            "2023-02-28,2.320000,2.470000,2.280000,2.460000,2.460000,2695237\n" +
            "2023-03-01,2.460000,2.460000,2.305000,2.330000,2.330000,1571404\n" +
            "2023-03-02,2.370000,2.390000,2.320000,2.360000,2.360000,1405591\n" +
            "2023-03-03,2.380000,2.380000,2.320000,2.370000,2.370000,1566201\n" +
            "2023-03-06,2.410000,2.430000,2.210000,2.230000,2.230000,2012616\n" +
            "2023-03-07,2.230000,2.235000,2.150000,2.220000,2.220000,2148047\n" +
            "2023-03-08,2.210000,2.340000,2.190000,2.340000,2.340000,1857253\n" +
            "2023-03-09,2.340000,2.360000,2.290000,2.290000,2.290000,1351348\n" +
            "2023-03-10,2.290000,2.290000,2.160000,2.160000,2.160000,1941727\n" +
            "2023-03-13,2.100000,2.175000,2.050000,2.140000,2.140000,2223825\n" +
            "2023-03-14,2.120000,2.195000,2.120000,2.140000,2.140000,1292019\n" +
            "2023-03-15,2.200000,2.235000,2.170000,2.170000,2.170000,1552973\n" +
            "2023-03-16,2.150000,2.190000,2.040000,2.060000,2.060000,2056312\n" +
            "2023-03-17,2.090000,2.160000,2.040000,2.040000,2.040000,31309969\n" +
            "2023-03-20,2.020000,2.110000,2.020000,2.070000,2.070000,2198007\n" +
            "2023-03-21,2.080000,2.110000,2.020000,2.020000,2.020000,1909199\n" +
            "2023-03-22,2.050000,2.075000,1.995000,2.070000,2.070000,2121566\n" +
            "2023-03-23,1.970000,1.970000,1.755000,1.815000,1.815000,6634345\n" +
            "2023-03-24,1.800000,1.815000,1.712500,1.775000,1.775000,4998881\n" +
            "2023-03-27,1.770000,1.842500,1.762500,1.810000,1.810000,2327598\n" +
            "2023-03-28,1.785000,1.820000,1.780000,1.795000,1.795000,1520254\n" +
            "2023-03-29,1.785000,1.800000,1.720000,1.720000,1.720000,2027144\n" +
            "2023-03-30,1.730000,1.775000,1.727500,1.730000,1.730000,2125346\n" +
            "2023-03-31,1.760000,1.795000,1.745000,1.790000,1.790000,1934210\n" +
            "2023-04-03,1.780000,1.810000,1.750000,1.780000,1.780000,1753005\n" +
            "2023-04-04,1.780000,1.822500,1.775000,1.815000,1.815000,1256935\n" +
            "2023-04-05,1.855000,1.880000,1.815000,1.850000,1.850000,1643671\n" +
            "2023-04-06,1.855000,1.890000,1.845000,1.845000,1.845000,1508644\n" +
            "2023-04-11,1.855000,1.895000,1.800000,1.810000,1.810000,1627350\n" +
            "2023-04-12,1.810000,1.825000,1.760000,1.775000,1.775000,1789066\n" +
            "2023-04-13,1.775000,1.827500,1.765000,1.815000,1.815000,1498632\n" +
            "2023-04-14,1.840000,1.862500,1.762500,1.765000,1.765000,1086793\n" +
            "2023-04-17,1.765000,1.790000,1.710000,1.715000,1.715000,1948722\n" +
            "2023-04-18,1.700000,1.710000,1.665000,1.705000,1.705000,1882146\n" +
            "2023-04-19,1.700000,1.725000,1.675000,1.680000,1.680000,1245167\n" +
            "2023-04-20,1.690000,1.705000,1.637500,1.645000,1.645000,1324260\n" +
            "2023-04-21,1.645000,1.655000,1.615000,1.620000,1.620000,1460692\n" +
            "2023-04-24,1.635000,1.715000,1.625000,1.700000,1.700000,1624656\n" +
            "2023-04-26,1.700000,1.705000,1.640000,1.640000,1.640000,2009942\n" +
            "2023-04-27,1.620000,1.680000,1.620000,1.665000,1.665000,1018942\n" +
            "2023-04-28,1.665000,1.670000,1.625000,1.630000,1.630000,1036411\n" +
            "2023-05-01,1.630000,1.630000,1.630000,1.630000,1.630000,0\n" +
            "2023-05-02,1.750000,1.815000,1.605000,1.615000,1.615000,6691943\n" +
            "2023-05-03,1.600000,1.620000,1.470000,1.515000,1.515000,5825011\n" +
            "2023-05-04,1.450000,1.520000,1.440000,1.465000,1.465000,4228421\n" +
            "2023-05-05,1.470000,1.530000,1.455000,1.485000,1.485000,2471753\n" +
            "2023-05-08,1.490000,1.500000,1.440000,1.450000,1.450000,2193849\n" +
            "2023-05-09,1.450000,1.470000,1.405000,1.420000,1.420000,2073699\n" +
            "2023-05-10,1.425000,1.435000,1.400000,1.405000,1.405000,1773488\n" +
            "2023-05-11,1.410000,1.420000,1.390000,1.390000,1.390000,1943085\n" +
            "2023-05-12,1.390000,1.415000,1.390000,1.400000,1.400000,1191499\n" +
            "2023-05-15,1.395000,1.405000,1.335000,1.365000,1.365000,2427032\n" +
            "2023-05-16,1.360000,1.405000,1.345000,1.345000,1.345000,1668992\n" +
            "2023-05-17,1.330000,1.350000,1.330000,1.330000,1.330000,1862739\n" +
            "2023-05-18,1.335000,1.365000,1.330000,1.345000,1.345000,1325101\n" +
            "2023-05-19,1.355000,1.415000,1.337500,1.405000,1.405000,2817668";

    public static BigDecimal[] PRICES = toClosingPrices("PNV",PRICE_STRING);

    public static List<Price> toPrices(String code, String priceString) {
        try {
            InputStream bais = new ByteArrayInputStream(priceString.getBytes());
            BufferedReader br = new BufferedReader(new InputStreamReader(bais));
            String line;
            List<Price> tmp = new ArrayList<>();
            while((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                tmp.add(Price.parseStrings(code, new String[]{fields[0],fields[1],fields[2],fields[3],fields[4],fields[6]}));
            }
//            Collections.reverse(tmp);
            return tmp;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static BigDecimal[] toClosingPrices(String code, String priceString) {
        return toPrices(code, priceString).stream().map(Price::get_close).collect(Collectors.toList()).toArray(BigDecimal[]::new);
    }

    @Test
    public void testRSI() {
        int period = 14;
        BigDecimal[] window = Arrays.copyOfRange(PRICES, PRICES.length - period, PRICES.length);
        Optional<BigDecimal> rsi = RSI.calculateRSI(Arrays.asList(window), period);
        assertTrue(rsi.isPresent());
        assertEquals(YAHOO_RSI.doubleValue(), rsi.orElseThrow().doubleValue(), YAHOO_RSI.doubleValue()*0.01);
    }

    @Test
    public void testLobf() {
        LinearRegression.LineOfBestFit lobf = LinearRegression.lobf(Arrays.asList(PRICES));
        assertNotNull(lobf);

        Optional<com.github.dfauth.ta.functions.ref.LinearRegression> ref = com.github.dfauth.ta.functions.ref.LinearRegression.calculate(Arrays.asList(PRICES), BigDecimal::doubleValue);
        assertEquals(BigDecimal.valueOf(ref.get().getSlope()), lobf.getSlope());
//        assertEquals(ref.intercept(), lobf.get().getIntercept().doubleValue(), ref.intercept()*0.01);
//        assertEquals(ref.R2(), lobf.get().getR2().doubleValue(), ref.R2()*0.01);
//        assertEquals(ref.slopeStdErr(), lobf.get().getSlopeStdErr().doubleValue(), ref.slopeStdErr()*0.01);
//        assertEquals(ref.interceptStdErr(), lobf.get().getInterceptStdErr().doubleValue(), ref.interceptStdErr()*0.01);
//        assertEquals(ref.predict(i.get()), lobf.get().predict(i.get()).doubleValue(), ref.predict(i.get())*0.01);
    }
}
