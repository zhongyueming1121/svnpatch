package com.ymz.svnauth;


import org.junit.Test;

import java.util.HashMap;

/**
 * @author ymz
 * @date 2021/11/25 10:18
 */
public class ProgramTest {

    @Test
    public void getSvnAuth() {
        HashMap<String, String> svnAuth = Program.getSvnAuth();
        System.out.println(svnAuth.toString());
    }
}