package com.tk.fcmb.utils;


import com.tk.fcmb.Entities.TransactionGraphData;
import com.tk.fcmb.Entities.UsersGraphData;
import com.tk.fcmb.Entities.dto.Response;
import com.tk.fcmb.Repositories.TransactionGraphDataRepository;
import com.tk.fcmb.Repositories.UsersGraphDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.IntStream;

@Service
public class GraphDbPopulator {

    @Autowired
    private TransactionGraphDataRepository transactionGraphDataRepository;

    @Autowired
    private UsersGraphDataRepository usersGraphDataRepository;

    public void dailyGraphPopulator(){
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/list/analyseTransactions");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("type", "COUNT_BY_DAY");
        builder.queryParams(params);

        ResponseEntity<Response> response = null;

        try{
            response = restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Response.class);
        }catch (Exception ex){
            System.out.println("An Error Occurred " + ex.getMessage());
            return;
        }

        LinkedHashMap hashMap = (LinkedHashMap) Objects.requireNonNull(response.getBody()).getResponseData();

        int[] dailySuccessFul = new int[7];
        int[] dailyFailed = new int[7];


        if ((int) hashMap.get("sundayCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("sunday");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    dailySuccessFul[0]++;
                }else if (list1.get("responseCode").equals("1")){
                    dailyFailed[0]++;
                }
            });
        }
        if ((int) hashMap.get("mondayCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("monday");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    dailySuccessFul[1]++;
                }else if (list1.get("responseCode").equals("1")){
                    dailyFailed[1]++;
                }
            });
        }
        if ((int) hashMap.get("tuesdayCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("tuesday");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    dailySuccessFul[2]++;
                }else if (list1.get("responseCode").equals("1")){
                    dailyFailed[2]++;
                }
            });
        }
        if ((int) hashMap.get("wednesdayCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("wednesday");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    dailySuccessFul[3]++;
                }else if (list1.get("responseCode").equals("1")){
                    dailyFailed[3]++;
                }
            });
        }
        if ((int) hashMap.get("thursdayCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("thursday");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    dailySuccessFul[4]++;
                }else if (list1.get("responseCode").equals("1")){
                    dailyFailed[4]++;
                }
            });
        }
        if ((int) hashMap.get("fridayCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("friday");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    dailySuccessFul[5]++;
                }else if (list1.get("responseCode").equals("1")){
                    dailyFailed[5]++;
                }
            });
        }
        if ((int) hashMap.get("saturdayCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("saturday");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    dailySuccessFul[6]++;
                }else if (list1.get("responseCode").equals("1")){
                    dailyFailed[6]++;
                }
            });
        }

        List<Integer> dailySuccessfulList = new ArrayList<>();
        List<Integer> dailyFailedList = new ArrayList<>();


        IntStream.range(0, dailySuccessFul.length).forEach(i -> {
            dailySuccessfulList.add(dailySuccessFul[i]);
            dailyFailedList.add(dailyFailed[i]);
        });


        TransactionGraphData transactionGraphData = transactionGraphDataRepository.findByIdentifier("graphData");

        if (transactionGraphData == null) {
            transactionGraphData = new TransactionGraphData();
            transactionGraphData.setIdentifier("graphData");

        }
        transactionGraphData.setDailySuccessful(dailySuccessfulList);
        transactionGraphData.setDailyFailed(dailyFailedList);
        transactionGraphDataRepository.save(transactionGraphData);

    }

    public void weeklyGraphPopulator(){
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/list/analyseTransactions");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("type", "COUNT_BY_WEEK");
        builder.queryParams(params);

        ResponseEntity<Response> response = null;

        try {
             response = restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Response.class);
        }catch (Exception ex){
            System.out.println("An error occurred " + ex.getMessage());
            return;
        }

        System.out.println(Objects.requireNonNull(response.getBody()).getResponseData().getClass().getName());

        LinkedHashMap hashMap = (LinkedHashMap) Objects.requireNonNull(response.getBody()).getResponseData();
        System.out.println(hashMap);

        int[] weeklySuccessFul = new int[5];
        int[] weeklyFailed = new int[5];

        if ((int) hashMap.get("week1Count") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("week1");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    weeklySuccessFul[0]++;
                }else if (list1.get("responseCode").equals("1")){
                    weeklyFailed[0]++;
                }
            });
        }
        if ((int) hashMap.get("week2Count") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("week2");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    weeklySuccessFul[1]++;
                }else if (list1.get("responseCode").equals("1")){
                    weeklyFailed[1]++;
                }
            });
        }
        if ((int) hashMap.get("week3Count") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("week3");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    weeklySuccessFul[2]++;
                }else if (list1.get("responseCode").equals("1")){
                    weeklyFailed[2]++;
                }
            });
        }
        if ((int) hashMap.get("week4Count") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("week4");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    weeklySuccessFul[3]++;
                }else if (list1.get("responseCode").equals("1")){
                    weeklyFailed[3]++;
                }
            });
        }
        if ((int) hashMap.get("week5Count") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("week5");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    weeklySuccessFul[4]++;
                }else if (list1.get("responseCode").equals("1")){
                    weeklyFailed[4]++;
                }
            });
        }

        List<Integer> weeklySuccessfulList = new ArrayList<>();
        List<Integer> weeklyFailedList = new ArrayList<>();

        IntStream.range(0, weeklyFailed.length).forEach(i -> {
            weeklyFailedList.add(weeklyFailed[i]);
            weeklySuccessfulList.add(weeklySuccessFul[i]);
        });

        TransactionGraphData transactionGraphData = transactionGraphDataRepository.findByIdentifier("graphData");
        transactionGraphData.setWeeklyFailed(weeklyFailedList);
        transactionGraphData.setWeeklySuccessful(weeklySuccessfulList);
        transactionGraphDataRepository.save(transactionGraphData);



    }

    public void monthlyGraphPopulator(){
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/list/analyseTransactions");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("type", "COUNT_BY_MONTH");
        builder.queryParams(params);

        ResponseEntity<Response> response = null;

        try {
            response = restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Response.class);
        }catch (Exception ex){
            System.out.println("An error occurred " + ex.getMessage());
            return;
        }

        System.out.println(Objects.requireNonNull(response.getBody()).getResponseData().getClass().getName());

        LinkedHashMap hashMap = (LinkedHashMap) Objects.requireNonNull(response.getBody()).getResponseData();
        System.out.println(hashMap);

        int[] monthLySuccessFul = new int[12];
        int[] monthlyFailed = new int[12];

        if ((int) hashMap.get("janCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("jan");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[0]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[0]++;
                }
            });
        }
        if ((int) hashMap.get("febCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("feb");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[1]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[1]++;
                }
            });
        }
        if ((int) hashMap.get("marCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("mar");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[2]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[2]++;
                }
            });
        }
        if ((int) hashMap.get("aprCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("apr");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[3]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[3]++;
                }
            });
        }
        if ((int) hashMap.get("mayCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("may");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[4]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[4]++;
                }
            });
        }
        if ((int) hashMap.get("junCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("jun");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[5]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[5]++;
                }
            });
        }
        if ((int) hashMap.get("julCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("jul");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[6]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[6]++;
                }
            });
        }
        if ((int) hashMap.get("augCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("aug");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[7]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[7]++;
                }
            });
        }
        if ((int) hashMap.get("sepCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("sep");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[8]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[8]++;
                }
            });
        }
        if ((int) hashMap.get("octCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("oct");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[9]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[9]++;
                }
            });
        }
        if ((int) hashMap.get("novCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("nov");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[10]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[10]++;
                }
            });
        }
        if ((int) hashMap.get("decCount") > 0 ){
            List<LinkedHashMap> list = (List<LinkedHashMap>) hashMap.get("dec");
            list.forEach(list1 -> {
                if ( (list1.get("responseCode").equals("0"))){
                    monthLySuccessFul[11]++;
                }else if (list1.get("responseCode").equals("1")){
                    monthlyFailed[11]++;
                }
            });
        }

        List<Integer> monthlySuccessfulList = new ArrayList<>();
        List<Integer> monthlyFailedList = new ArrayList<>();

        IntStream.range(0, monthlyFailed.length).forEach(i -> {
            monthlyFailedList.add(monthlyFailed[i]);
            monthlySuccessfulList.add(monthLySuccessFul[i]);
        });

        TransactionGraphData transactionGraphData = transactionGraphDataRepository.findByIdentifier("graphData");
        transactionGraphData.setMonthlyFailed(monthlyFailedList);
        transactionGraphData.setMonthlySuccessful(monthlySuccessfulList);
        transactionGraphDataRepository.save(transactionGraphData);





    }

    public void yearlyGraphPopulator(){

        TransactionGraphData graphData = transactionGraphDataRepository.findByIdentifier("graphData");
        List<Integer> yearlySuccessfulList = new ArrayList<>();
        yearlySuccessfulList.add(graphData.getMonthlySuccessful().stream().mapToInt(Integer::intValue).sum());
        List<Integer> yearlyFailedList = new ArrayList<>();
        yearlyFailedList.add(graphData.getMonthlyFailed().stream().mapToInt(Integer::intValue).sum());

        graphData.setYearlyFailed(yearlyFailedList);
        graphData.setYearlySuccessful(yearlySuccessfulList);
        transactionGraphDataRepository.save(graphData);


    }

    public void userDailyGraphPopulator(){
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/list/analyseUsers");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("type", "COUNT_BY_DAY");
        builder.queryParams(params);

        ResponseEntity<Response> response = null;

        try {
            response = restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Response.class);
        }catch (Exception ex){
            System.out.println("An error occurred " + ex.getMessage());
            return;
        }

        System.out.println(Objects.requireNonNull(response.getBody()).getResponseData().getClass().getName());

        LinkedHashMap hashMap = (LinkedHashMap) Objects.requireNonNull(response.getBody()).getResponseData();
        System.out.println(hashMap);




        int[] dailySuccessFul = new int[7];


        if ((int) hashMap.get("sundayCount") > 0 ){
            dailySuccessFul[0] = (int) hashMap.get("sundayCount");
        }
        if ((int) hashMap.get("mondayCount") > 0 ){
            dailySuccessFul[1] = (int) hashMap.get("mondayCount");
        }
        if ((int) hashMap.get("tuesdayCount") > 0 ){
            dailySuccessFul[2] = (int) hashMap.get("tuesdayCount");
        }
        if ((int) hashMap.get("wednesdayCount") > 0 ){
            dailySuccessFul[3] = (int) hashMap.get("wednesdayCount");
        }
        if ((int) hashMap.get("thursdayCount") > 0 ){
            dailySuccessFul[4] = (int) hashMap.get("thursdayCount");
        }
        if ((int) hashMap.get("fridayCount") > 0 ){
            dailySuccessFul[5] = (int) hashMap.get("fridayCount");
        }
        if ((int) hashMap.get("saturdayCount") > 0 ){
            dailySuccessFul[6] = (int) hashMap.get("saturdayCount");
        }

        List<Integer> dailySuccessfulList = new ArrayList<>();


        IntStream.range(0, dailySuccessFul.length).forEach(i -> {
            dailySuccessfulList.add(dailySuccessFul[i]);
        });


        if (usersGraphDataRepository.count() == 0) {

            UsersGraphData usersGraphData = new UsersGraphData();
            usersGraphData.setIdentifier("graphData");
            usersGraphData.setDailySuccessful(dailySuccessfulList);
            usersGraphDataRepository.save(usersGraphData);
        }else{
            UsersGraphData usersGraphData = usersGraphDataRepository.findByIdentifier("graphData");
            usersGraphData.setDailySuccessful(dailySuccessfulList);
            usersGraphDataRepository.save(usersGraphData);
        }


    }

    public void userWeeklyGraphPopulator(){
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/list/analyseUsers");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("type", "COUNT_BY_WEEK");
        builder.queryParams(params);

        ResponseEntity<Response> response = null;

        try {
            response = restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Response.class);
        }catch (Exception ex){
            System.out.println("An error occurred " + ex.getMessage());
            return;
        }

        System.out.println(Objects.requireNonNull(response.getBody()).getResponseData().getClass().getName());

        LinkedHashMap hashMap = (LinkedHashMap) Objects.requireNonNull(response.getBody()).getResponseData();
        System.out.println(hashMap);

        int[] weeklySuccessFul = new int[5];

        if ((int) hashMap.get("week1Count") > 0 ){
            weeklySuccessFul[0] = (int) hashMap.get("week1Count");
        }
        if ((int) hashMap.get("week2Count") > 0 ){
            weeklySuccessFul[1] = (int) hashMap.get("week2Count");
        }
        if ((int) hashMap.get("week3Count") > 0 ){
            weeklySuccessFul[2] = (int) hashMap.get("week3Count");
        }
        if ((int) hashMap.get("week4Count") > 0 ){
            weeklySuccessFul[3] = (int) hashMap.get("week4Count");
        }
        if ((int) hashMap.get("week5Count") > 0 ){
            weeklySuccessFul[4] = (int) hashMap.get("week5Count");
        }

        List<Integer> weeklySuccessfulList = new ArrayList<>();

        IntStream.range(0, weeklySuccessFul.length).forEach(i -> {
            weeklySuccessfulList.add(weeklySuccessFul[i]);
        });

        UsersGraphData usersGraphData = usersGraphDataRepository.findByIdentifier("graphData");
        usersGraphData.setWeeklySuccessful(weeklySuccessfulList);
        usersGraphDataRepository.save(usersGraphData);



    }

    public void usersMonthlyGraphPopulator(){
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/list/analyseUsers");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("type", "COUNT_BY_MONTH");
        builder.queryParams(params);

        ResponseEntity<Response> response = null;

        try {
            response = restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Response.class);
        }catch (Exception ex){
            System.out.println("An error occurred " + ex.getMessage());
            return;
        }

        System.out.println(Objects.requireNonNull(response.getBody()).getResponseData().getClass().getName());

        LinkedHashMap hashMap = (LinkedHashMap) Objects.requireNonNull(response.getBody()).getResponseData();
        System.out.println(hashMap);

        int[] monthLySuccessFul = new int[12];

        if ((int) hashMap.get("janCount") > 0 ){
            monthLySuccessFul[0] = (int) hashMap.get("janCount");
        }
        if ((int) hashMap.get("febCount") > 0 ){
            monthLySuccessFul[1] = (int) hashMap.get("febCount");
        }
        if ((int) hashMap.get("marCount") > 0 ){
            monthLySuccessFul[2] = (int) hashMap.get("marCount");
        }
        if ((int) hashMap.get("aprCount") > 0 ){
            monthLySuccessFul[3] = (int) hashMap.get("aprCount");
        }
        if ((int) hashMap.get("mayCount") > 0 ){
            monthLySuccessFul[4] = (int) hashMap.get("mayCount");
        }
        if ((int) hashMap.get("junCount") > 0 ){
            monthLySuccessFul[5] = (int) hashMap.get("junCount");
        }
        if ((int) hashMap.get("julCount") > 0 ){
            monthLySuccessFul[6] = (int) hashMap.get("julCount");
        }
        if ((int) hashMap.get("augCount") > 0 ){
            monthLySuccessFul[7] = (int) hashMap.get("augCount");
        }
        if ((int) hashMap.get("sepCount") > 0 ){
            monthLySuccessFul[8] = (int) hashMap.get("sepCount");
        }
        if ((int) hashMap.get("octCount") > 0 ){
            monthLySuccessFul[9] = (int) hashMap.get("octCount");
        }
        if ((int) hashMap.get("novCount") > 0 ){
            monthLySuccessFul[10] = (int) hashMap.get("novCount");
        }
        if ((int) hashMap.get("decCount") > 0 ){
            monthLySuccessFul[11] = (int) hashMap.get("decCount");
        }

        List<Integer> monthlySuccessfulList = new ArrayList<>();

        IntStream.range(0, monthLySuccessFul.length).forEach(i -> {
            monthlySuccessfulList.add(monthLySuccessFul[i]);
        });

        UsersGraphData usersGraphData = usersGraphDataRepository.findByIdentifier("graphData");
        usersGraphData.setMonthlySuccessful(monthlySuccessfulList);
        usersGraphDataRepository.save(usersGraphData);





    }

    public void usersYearlyGraphPopulator(){

        UsersGraphData graphData = usersGraphDataRepository.findByIdentifier("graphData");
        List<Integer> yearlySuccessfulList = new ArrayList<>();
        yearlySuccessfulList.add(graphData.getMonthlySuccessful().stream().mapToInt(Integer::intValue).sum());
        graphData.setYearlySuccessful(yearlySuccessfulList);
        usersGraphDataRepository.save(graphData);


    }

    public void usersDaysInWeeksGraphPopulator(){
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://18.190.12.249:8001/FCMBProcessor/list/analyseUsers");

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("type", "COUNT_BY_DAYS_IN_WEEKS");
        builder.queryParams(params);

        ResponseEntity<Response> response = null;

        try {
            response = restTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Response.class);
        }catch (Exception ex){
            System.out.println("An error occurred " + ex.getMessage());
            return;
        }

        LinkedHashMap hashMap = (LinkedHashMap) Objects.requireNonNull(response.getBody()).getResponseData();

        LinkedHashMap week1Data = (LinkedHashMap) hashMap.get("week1");
        LinkedHashMap week2Data = (LinkedHashMap) hashMap.get("week2");
        LinkedHashMap week3Data = (LinkedHashMap) hashMap.get("week3");
        LinkedHashMap week4Data = (LinkedHashMap) hashMap.get("week4");
        LinkedHashMap week5Data = (LinkedHashMap) hashMap.get("week5");


        int[] week1 = new int[7];
        int[] week2 = new int[7];
        int[] week3 = new int[7];
        int[] week4 = new int[7];
        int[] week5 = new int[7];

        List<Integer> week1List = new ArrayList<>();
        List<Integer> week2List = new ArrayList<>();
        List<Integer> week3List = new ArrayList<>();
        List<Integer> week4List = new ArrayList<>();
        List<Integer> week5List = new ArrayList<>();

        check(week1Data,week1);
        check(week2Data,week2);
        check(week3Data,week3);
        check(week4Data,week4);
        check(week5Data,week5);

        IntStream.range(0, week1.length).forEach(i -> {
            week1List.add(week1[i]);
            week2List.add(week2[i]);
            week3List.add(week3[i]);
            week4List.add(week4[i]);
            week5List.add(week5[i]);
        });

        UsersGraphData usersGraphData = usersGraphDataRepository.findByIdentifier("graphData");
        usersGraphData.setWeek1Successful(week1List);
        usersGraphData.setWeek2Successful(week2List);
        usersGraphData.setWeek3Successful(week3List);
        usersGraphData.setWeek4Successful(week4List);
        usersGraphData.setWeek5Successful(week5List);
        usersGraphDataRepository.save(usersGraphData);

    }

    private void check(LinkedHashMap weekData, int[] weekArray){
        if (weekData != null){
            if ((int) weekData.get("sundayCount") > 0){
                weekArray[0] = (int) weekData.get("sundayCount");
            }
            if ((int) weekData.get("mondayCount") > 0){
                weekArray[1] = (int) weekData.get("mondayCount");
            }
            if ((int) weekData.get("tuesdayCount") > 0){
                weekArray[2] = (int) weekData.get("tuesdayCount");
            }
            if ((int) weekData.get("wednesdayCount") > 0){
                weekArray[3] = (int) weekData.get("wednesdayCount");
            }
            if ((int) weekData.get("thursdayCount") > 0){
                weekArray[4] = (int) weekData.get("thursdayCount");
            }
            if ((int) weekData.get("fridayCount") > 0){
                weekArray[5] = (int) weekData.get("fridayCount");
            }
            if ((int) weekData.get("saturdayCount") > 0){
                weekArray[6] = (int) weekData.get("saturdayCount");
            }
        }
    }
}
