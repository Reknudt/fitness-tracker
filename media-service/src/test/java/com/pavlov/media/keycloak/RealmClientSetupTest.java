package com.pavlov.media.keycloak;

import com.pavlov.media.serviceKeycloak.service.KeycloakSetupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RealmClientSetupTest {

    @Autowired
    private Keycloak keycloak;
    @Autowired
    private KeycloakSetupService keycloakSetupService;
    private final String REALM = "scassets";
    private final String CLIENT = "scassets-client";

    @Test
    void login() {
//        keycloak.realm(REALM).

    }

    @Test
    @DisplayName("Should create realm and client")
    void createRealm_ShouldCreateRealmAndClient() {
//        try (Response response = keycloakSetupService.createRealmWithClient()) {
        try {
            keycloakSetupService.createRealmWithClient();
//                assertEquals(CREATED.getStatusCode(), response.getStatus());
            assertTrue(keycloakSetupService.realmExists());
            RealmResource realmResource = keycloak.realm(REALM);
            ClientsResource clientsResource = realmResource.clients();
            assertNotNull(keycloakSetupService.findClientByClientId(clientsResource));
        } finally {
            RealmResource realmResource = keycloak.realm(REALM);
//            realmResource.remove();
        }
    }

    @Test
    @DisplayName("Should throw conflict")
    void createRealm_ShouldThrowConflict() {
//        try (Response response = keycloakSetupService.createRealmWithClient()) {
        try {
            keycloakSetupService.createRealmWithClient();
//            assertEquals(CREATED.getStatusCode(), response.getStatus());

            ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> keycloakSetupService.createRealmWithClient());
            assertEquals("Client already set up", e.getReason());
        } finally {
            RealmResource realmResource = keycloak.realm(REALM);
            realmResource.remove();
        }
    }

    /*@Test
    @DisplayName("Should reset realm and set client")
    void createRealm_ShouldResetRealmAndSetClient() {
        try (Response response = keycloakSetupService.createRealmWithClient()) {
            assertEquals(CREATED.getStatusCode(), response.getStatus());
            String location = response.getLocation().getPath();
            keycloak.realm(REALM).clients().get(location.substring(location.lastIndexOf('/') + 1)).remove();

            keycloakSetupService.createRealmWithClient();

            assertTrue(keycloakSetupService.realmExists());
            RealmResource realmResource = keycloak.realm(REALM);
            ClientsResource clientsResource = realmResource.clients();
            assertNotNull(keycloakSetupService.findClientByClientId(clientsResource));
        } finally {
            RealmResource realmResource = keycloak.realm(REALM);
            realmResource.remove();
        }
    }*/

    //    @Test
    @DisplayName("Remove REALM, optional test")
    void removeRealm() {
        RealmResource realmResource = keycloak.realm(REALM);
        realmResource.remove();
    }

    @Test
    void findSubStr() {
//        String s = "02234568890";
        String s = "11111112";
        List<Integer> inds = new java.util.ArrayList<>(List.of(0));
        for (int i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) == s.charAt(i + 1)) {
                inds.add(i + 1);
            }
        }
//        inds.add(s.length());
        int longest = 0;
        for (int i = 0; i < inds.size() - 2; i++) {
            if (inds.get(i + 2) - inds.get(i) > longest) {
                longest = inds.get(i + 2) - inds.get(i);
            }
        }
        if (longest == 0) {
            System.out.println("longest is all: " + s.length());
        }
        System.out.println("longest: " + longest);

    }

    @Test
    void numsList() {
//        List<Integer> nums = List.of(3,2,2,3,4);
        int[] arr1 = new int[]{13, 27, 45};
        int[] arr2 = new int[]{21, 27, 48};
        int l = 0;
        HashSet<Integer> hset = new HashSet<>();
        for (int k : arr1) {
            int i1 = k;
            while (i1 > 0) {
                hset.add(i1);
                i1 = i1 / 10;
            }
        }
        int res = 0;
        for (int j : arr2) {
            int i1 = j;
            while (i1 > 0) {
                l = 0;
                if (hset.contains(i1)) {
                    while (i1 > 0) {
                        int buf = i1 / 10;
                        l++;
                        if (buf == 0 && l > res) {
                            res = l;
//                            System.out.println("found: " + l);
                        }
                        i1 = buf;
                    }
                }
                i1 = i1 / 10;
            }
        }
        System.out.println("length: " + res);

        boolean fl = true;
        Map<Integer, Integer> stateToIndex = new HashMap<>();
    }

    @Test
    void getSort() {
        List<Integer> nums = new ArrayList<>(List.of(1, 3, 2, 3, 1, 3));

//        Map<Integer, List<Integer>> map = new HashMap<>();
//        for (int i = 0; i < nums.size(); i++) {
//            List<Integer> count = map.putIfAbsent(nums.get(i), List.of(i));
//            if (count != null) {
//                map.remove(map.get(i));
//                count.add(i);
//                map.put(nums.get(i), count);
//            }
//        }
//        System.out.println("3 occures in: " + map.get(3));

        Map<Integer, Integer> map2 = new HashMap<>();
        for (int i = 0; i < nums.size(); i++) {
            Integer count = map2.putIfAbsent(nums.get(i), 1);
            if (count != null) {
                map2.remove(map2.get(i));
                count++;
                map2.put(nums.get(i), count);
            }
        }
//        for ()
//        System.out.println("3 occures: " + map2.get(3));
    }

    @Test
    void testArr() {
        /*har a = 'a';
        char b = 'b';
        char A = 'A';
        char B = 'B';
        System.out.println("a: " + ((int) a - 96) + " , b: " + ((int) b - 96) + " , A: " + ((int) A - 63) + " , B " + ((int) B - 63));

        String str = "A";
        int valA = 0;
        if (Character.isUpperCase(A)) {
            valA = (Character.toLowerCase(A) - 96) * 2;
        }
        System.out.println("valA: " + valA);*/

        char i1 = '1';
        char i5 = '5';
        int digit = i1;
        int digit5 = i5;
        System.out.println("i1 " + digit + " , i5 " + digit5);

        String s = "Give me 5!";

        char[] str = s.toCharArray();
        int sum = 0;
        for (char c : str) {
            if (Character.isLowerCase(c)) {
                sum += c - 96;
                System.out.println("lc sum: " + sum + " symbol " + c);
            } else if (Character.isUpperCase(c)) {
                sum += (Character.toLowerCase(c) - 96) * 2;
                System.out.println("uc sum: " + sum + " symbol " + c);
            } else if (Character.isDigit(c)) {
                sum = c;
                System.out.println("digit sum: " + sum + " symbol " + c);
            }
        }
        System.out.println("sum: " + s);
    }
}
