/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.finominfo.rnet.frontend.servant.gameknock;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author User
 */
public class Test {

    public static void main(String[] args) {
        List<String> morseList = Arrays.asList(("0,1").split(","));
        System.out.println(morseList.stream().map(Integer::parseInt).collect(Collectors.toList()));
    }
}
