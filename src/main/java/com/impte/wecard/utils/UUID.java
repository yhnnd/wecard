package com.impte.wecard.utils;

import java.util.ArrayList;
import java.util.List;

public class UUID {
    public static String getUUID(){
        String uuid = java.util.UUID.randomUUID().toString();
        uuid = changeBand16to2(uuid);
        uuid = changeBand2to64(uuid);
        return uuid;
    }

    public static String changeBand16to64(String str){
        str = str.toLowerCase();
        str = changeBand16to2(str);
        str = changeBand2to64(str);
        return str;
    }


    private static String changeBand16to2(String number){
        char[] chs = number.toCharArray();
        String result = "";
        for (char ch: chs){
            String item = String.valueOf(ch);
            if (item.equals("0")){
                result = result + "0000";
            }else if (item.equals("1")){
                result = result + "0001";
            }else if (item.equals("2")){
                result = result + "0010";
            }else if (item.equals("3")){
                result = result + "0011";
            }else if (item.equals("4")){
                result = result + "0100";
            }else if (item.equals("5")){
                result = result + "0101";
            }else if (item.equals("6")){
                result = result + "0110";
            }else if (item.equals("7")){
                result = result + "0111";
            }else if (item.equals("8")){
                result = result + "1000";
            }else if (item.equals("9")){
                result = result + "1001";
            }else if (item.equals("a")){
                result = result + "1010";
            }else if (item.equals("b")){
                result = result + "1011";
            }else if (item.equals("c")){
                result = result + "1100";
            }else if (item.equals("d")){
                result = result + "1101";
            }else if (item.equals("e")){
                result = result + "1110";
            }else if (item.equals("f")){
                result = result + "1111";
            }
        }
        return result;
    }

    private static String changeBand2to64(String number){
        int remainder = number.length()%6;
        if(remainder > 0){
            for (int i=(6-remainder); i>0; i--){
                number = "0" + number;
            }
        }

        List<String> binaries = new ArrayList<>();
        if (number == null || "".equals(number))
            return null;
        if ((number.length() % 6) != 0)
            return null;
        for (int i = 0; i < number.length(); i += 6) {
            binaries.add(number.substring(i, i + 6));
        }
        String item = "";
        String result = "";
        for (String binary: binaries){
            if ("000000".equals(binary)) {
                item = "0";
            } else if ("000001".equals(binary)) {
                item = "1";
            } else if ("000010".equals(binary)) {
                item = "2";
            } else if ("000011".equals(binary)) {
                item = "3";
            } else if ("000100".equals(binary)) {
                item = "4";
            } else if ("000101".equals(binary)) {
                item = "5";
            } else if ("000110".equals(binary)) {
                item = "6";
            } else if ("000111".equals(binary)) {
                item = "7";
            } else if ("001000".equals(binary)) {
                item = "8";
            } else if ("001001".equals(binary)) {
                item = "9";
            } else if ("001010".equals(binary)) {
                item = "a";
            } else if ("001011".equals(binary)) {
                item = "b";
            } else if ("001100".equals(binary)) {
                item = "c";
            } else if ("001101".equals(binary)) {
                item = "d";
            } else if ("001110".equals(binary)) {
                item = "e";
            } else if ("001111".equals(binary)) {
                item = "f";
            } else if ("010000".equals(binary)) {
                item = "g";
            } else if ("010001".equals(binary)) {
                item = "h";
            } else if ("010010".equals(binary)) {
                item = "i";
            } else if ("010011".equals(binary)) {
                item = "j";
            } else if ("010100".equals(binary)) {
                item = "k";
            } else if ("010101".equals(binary)) {
                item = "l";
            } else if ("010110".equals(binary)) {
                item = "m";
            } else if ("010111".equals(binary)) {
                item = "n";
            } else if ("011000".equals(binary)) {
                item = "o";
            } else if ("011001".equals(binary)) {
                item = "p";
            } else if ("011010".equals(binary)) {
                item = "q";
            } else if ("011011".equals(binary)) {
                item = "r";
            } else if ("011100".equals(binary)) {
                item = "s";
            } else if ("011101".equals(binary)) {
                item = "t";
            } else if ("011110".equals(binary)) {
                item = "u";
            } else if ("011111".equals(binary)) {
                item = "v";
            } else if ("100000".equals(binary)) {
                item = "w";
            } else if ("100001".equals(binary)) {
                item = "x";
            } else if ("100010".equals(binary)) {
                item = "y";
            } else if ("100011".equals(binary)) {
                item = "z";
            } else if ("100100".equals(binary)) {
                item = "A";
            } else if ("100101".equals(binary)) {
                item = "B";
            } else if ("100110".equals(binary)) {
                item = "C";
            } else if ("100111".equals(binary)) {
                item = "D";
            } else if ("101000".equals(binary)) {
                item = "E";
            } else if ("101001".equals(binary)) {
                item = "F";
            } else if ("101010".equals(binary)) {
                item = "G";
            } else if ("101011".equals(binary)) {
                item = "H";
            } else if ("101100".equals(binary)) {
                item = "I";
            } else if ("101101".equals(binary)) {
                item = "J";
            } else if ("101110".equals(binary)) {
                item = "K";
            } else if ("101111".equals(binary)) {
                item = "L";
            } else if ("110000".equals(binary)) {
                item = "M";
            } else if ("110001".equals(binary)) {
                item = "N";
            } else if ("110010".equals(binary)) {
                item = "O";
            } else if ("110011".equals(binary)) {
                item = "P";
            } else if ("110100".equals(binary)) {
                item = "Q";
            } else if ("110101".equals(binary)) {
                item = "R";
            } else if ("110110".equals(binary)) {
                item = "S";
            } else if ("110111".equals(binary)) {
                item = "T";
            } else if ("111000".equals(binary)) {
                item = "U";
            } else if ("111001".equals(binary)) {
                item = "V";
            } else if ("111010".equals(binary)) {
                item = "W";
            } else if ("111011".equals(binary)) {
                item = "X";
            } else if ("111100".equals(binary)) {
                item = "Y";
            } else if ("111101".equals(binary)) {
                item = "Z";
            } else if ("111110".equals(binary)) {
                item = "_";
            } else if ("111111".equals(binary)) {
                item = "-";
            }
            result = result + item;
        }

        return result;
    }
}
