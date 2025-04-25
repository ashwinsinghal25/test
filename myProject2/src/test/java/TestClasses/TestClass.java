package TestClasses;

import api.Headers;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import repository.constant.ApiEndPoint;
import utils.*;
import java.sql.ResultSet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;

public class TestClass extends TestBase {

    private String jwt;
    PropertyFactory dataProperties;
    String client_id;
    String agentId;

    String secret_key;
    String base_url;
    String fromdate;
    String todate;
    String bodyAdas;
    String agentProfileId;
    RestAssuredRequestUtil request;
    MySQLQueryUtil mysqlutil;

    GetDataFromDB getDataFromDBUtils = new GetDataFromDB();
    Map<String, String> jwtParams = new HashMap<>();

    String newFSEProfileId;
    String newFSEEmpId;
    String newFSEMobileNumber;
    String newFseInfoId;
    String newFSECustId;

    String newTLProfileId;
    String newTLEmpId;
    String newTLMobileNumber;
    String newTLFseInfoId;

    String newTLCustId;
    String newPGMid;
    HelperUtil helperUtil;

    BeatCreationUtil beatCreationUtil;

    @BeforeClass
    public void beforeClass() throws Exception
    {
        dataProperties = Propertyfile.getDBProperty();
        newFSEMobileNumber=dataProperties.getProperty("newfse1.mobileNumber");
        newFSEProfileId= dataProperties.getProperty("newfse1.profile.id");
        newFseInfoId= dataProperties.getProperty("newfse1.fseInfo.id");
        newFSEEmpId= dataProperties.getProperty("newfse1.emp.id");
        newFSECustId= dataProperties.getProperty("newfse1.custId");
        newPGMid= dataProperties.getProperty("newpgMid1");
        helperUtil= new HelperUtil();
        beatCreationUtil=new BeatCreationUtil();
        newTLMobileNumber= dataProperties.getProperty("newtl1.mobileNumber");
        newTLProfileId = dataProperties.getProperty("newtl1.profile.id");
        newTLEmpId= dataProperties.getProperty("newtl1.emp.id");
        newTLFseInfoId= dataProperties.getProperty("newtl1.fseInfo.id");
        newTLCustId= dataProperties.getProperty("newtl1.custId");
        client_id = dataProperties.getProperty("CLIENT_ID");
        secret_key = dataProperties.getProperty("SECRET_KEY");
        base_url = dataProperties.getProperty("Agent_360_BASE_URL");
        agentId = dataProperties.getProperty("agentId_For_ADAS");
        agentProfileId = dataProperties.getProperty("newfse1.profile.id");
        LocalDateTime ldt = LocalDateTime.now();
        fromdate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(ldt);
        todate = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH).format(ldt);
        jwt = jwtTokenValue;
        request = new RestAssuredRequestUtil(base_url);
        mysqlutil = new MySQLQueryUtil();

    }

    @Test(description = "Fetch Regularization Dates of a valid employee", groups = "P0")
    public void TC01_ValidEmployee_P0() throws Exception {

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newTLEmpId);

        // Make API Call
        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates, queryParams, Headers.getHeadersAgent360(newTLCustId),200);
        System.out.println("Regularization Dates API Response: "+ response);

    }
    @Test(description = "Fetch Regularization Dates for Valid FSE", groups = "P0")
    public void TC01_ValidFSE0() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertNotNull(jsonPath.get("data.regularizationDates"));

        // Validate DB
        String query = String.format("SELECT COUNT(*) as count FROM attendance_regularization WHERE emp_id = '%s'",
                newFSEEmpId);
        ResultSet rs = getDataFromDBUtils.getRowDataFromDB(dbBase, query);
        Assert.assertTrue(rs.next(), "No regularization records found in DB");
    }

    @Test(description = "Fetch Regularization Dates for Valid TL", groups = "P0")
    public void TC02_ValidTL0() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newTLEmpId);

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newTLCustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertNotNull(jsonPath.get("data.regularizationDates"));
    }

    @Test(description = "Fetch Regularization Dates with Invalid Employee Code", groups = "P0")
    public void TC03_InvalidEmployeeCode0() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", "INVALID_EMP_CODE");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newTLCustId),
                400);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertFalse(jsonPath.getBoolean("success"));
        Assert.assertEquals(jsonPath.getString("errorMessage"), "Invalid employee code");
    }

    @Test(description = "Fetch Regularization Dates with Missing Headers", groups = "P0")
    public void TC04_MissingHeaders0() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                null,
                400);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertEquals(jsonPath.getString("statusMessage"),
                "Required request header 'userId' for method parameter type Long is not present");
    }

    @Test(description = "Fetch Regularization Dates with Missing Parameters", groups = "P0")
    public void TC05_MissingParameters0() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        // Intentionally leaving parameters empty

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newTLCustId),
                400);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertFalse(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getString("errorMessage").contains("Required parameter 'empCode' is missing"));
    }

    @Test(description = "Fetch Regularization Dates for Inactive Employee", groups = "P1")
    public void TC06_InactiveEmployee1() throws Exception {
        String inactiveEmpCode = "INACTIVE_EMP_CODE";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", inactiveEmpCode);

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newTLCustId),
                400);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertFalse(jsonPath.getBoolean("success"));
        Assert.assertEquals(jsonPath.getString("errorMessage"), "Employee is not active");
    }

    @Test(description = "Verify Cross-Role Access for Regularization Dates", groups = "P1")
    public void TC07_CrossRoleAccess1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newTLEmpId);

        // Trying to access TL's data using FSE's credentials
        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                403);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertFalse(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getString("errorMessage").contains("Unauthorized access"));
    }

    @Test(description = "Verify Response Time for Regularization Dates", groups = "P1")
    public void TC08_ResponseTime1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);

        long startTime = System.currentTimeMillis();

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        Assert.assertTrue(responseTime < 2000, "Response time exceeded 2 seconds");

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
    }

    @Test(description = "Verify SQL Injection Prevention", groups = "P1")
    public void TC09_SQLInjectionAttempt1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", "1' OR '1'='1"); // SQL injection attempt

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newTLCustId),
                400);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertFalse(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getString("errorMessage").contains("Invalid employee code format"));
    }

    @Test(description = "Verify Special Characters in Employee Code", groups = "P1")
    public void TC10_SpecialCharactersEmpCode1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", "123@#$%");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newTLCustId),
                400);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertFalse(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getString("errorMessage").contains("Invalid employee code format"));
    }

    @Test(description = "Verify Regularization Dates with Date Range", groups = "P1")
    public void TC11_DateRangeValidation1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("fromDate", "2024-01-01");
        queryParams.put("toDate", "2024-01-31");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getList("data.regularizationDates").size() <= 31);
    }

    @Test(description = "Verify Regularization Dates with Invalid Date Range", groups = "P1")
    public void TC12_InvalidDateRange1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("fromDate", "2024-01-31");
        queryParams.put("toDate", "2024-01-01"); // End date before start date

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                400);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertFalse(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getString("errorMessage").contains("Invalid date range"));
    }

    @Test(description = "Verify Regularization Dates for Weekend", groups = "P1")
    public void TC13_WeekendDates1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("date", "2024-03-16"); // Saturday

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getBoolean("data.isWeekend"));
    }

    @Test(description = "Verify Regularization Dates for Holiday", groups = "P1")
    public void TC14_HolidayDates1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("date", "2024-01-26"); // Republic Day

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getBoolean("data.isHoliday"));
    }

    @Test(description = "Verify Maximum Date Range Limit", groups = "P1")
    public void TC15_MaxDateRangeLimit1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("fromDate", "2023-01-01");
        queryParams.put("toDate", "2024-01-01"); // More than 6 months

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                400);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertFalse(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getString("errorMessage").contains("Date range exceeds maximum limit"));
    }

    @Test(description = "Verify Regularization Status Updates", groups = "P1")
    public void TC16_StatusUpdates1() throws Exception {
        // First, get current regularization dates
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));

        // Validate status transitions in DB
        String query = String.format("SELECT status_history FROM attendance_regularization WHERE emp_id = '%s' ORDER BY updated_at DESC LIMIT 1",
                newFSEEmpId);
        ResultSet rs = getDataFromDBUtils.getRowDataFromDB(dbBase, query);
        Assert.assertTrue(rs.next(), "No status history found");
    }

    @Test(description = "Verify Concurrent Regularization Requests", groups = "P1")
    public void TC17_ConcurrentRequests1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);

        // Make multiple concurrent requests
        long startTime = System.currentTimeMillis();

        Thread thread1 = new Thread(() -> {
            try {
                request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                        queryParams,
                        Headers.getHeadersAgent360(newFSECustId),
                        200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                        queryParams,
                        Headers.getHeadersAgent360(newFSECustId),
                        200);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        Assert.assertTrue(totalTime < 4000, "Concurrent requests took too long");
    }

    @Test(description = "Verify Cache Invalidation", groups = "P1")
    public void TC18_CacheInvalidation1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("bypassCache", "true");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertNotNull(jsonPath.get("data.regularizationDates"));
    }

    @Test(description = "Verify Response Pagination", groups = "P1")
    public void TC19_ResponsePagination1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("page", "1");
        queryParams.put("size", "10");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getList("data.regularizationDates").size() <= 10);
        Assert.assertNotNull(jsonPath.get("data.pagination"));
    }

    @Test(description = "Verify Data Consistency", groups = "P1")
    public void TC20_DataConsistency1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);

        // Make first request
        String response1 = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        // Make second request
        String response2 = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath1 = new JsonPath(response1);
        JsonPath jsonPath2 = new JsonPath(response2);

        // Compare responses
        Assert.assertEquals(jsonPath1.getList("data.regularizationDates"),
                jsonPath2.getList("data.regularizationDates"),
                "Inconsistent data between requests");
    }

    @Test(description = "Verify Regularization Dates with Multiple Status Filter", groups = "P1")
    public void TC21_MultipleStatusFilter1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("status", "PENDING,APPROVED,REJECTED");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertNotNull(jsonPath.get("data.regularizationDates"));
    }

    @Test(description = "Verify Regularization Dates with Invalid Status", groups = "P1")
    public void TC22_InvalidStatus1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("status", "INVALID_STATUS");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                400);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertFalse(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getString("errorMessage").contains("Invalid status value"));
    }

    @Test(description = "Verify Regularization Dates with Reason Filter", groups = "P1")
    public void TC23_ReasonFilter1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("reason", "SICK_LEAVE");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertNotNull(jsonPath.get("data.regularizationDates"));
    }

    @Test(description = "Verify Regularization Dates Sort Order", groups = "P1")
    public void TC24_SortOrder1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("sortBy", "date");
        queryParams.put("sortOrder", "DESC");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        // Verify dates are in descending order
        List<String> dates = jsonPath.getList("data.regularizationDates.date");
        Assert.assertTrue(isDatesSorted(dates, false), "Dates are not in descending order");
    }

    private boolean isDatesSorted(List<String> dates, boolean ascending) {
        for (int i = 0; i < dates.size() - 1; i++) {
            LocalDate date1 = LocalDate.parse(dates.get(i));
            LocalDate date2 = LocalDate.parse(dates.get(i + 1));
            if (ascending) {
                if (date1.compareTo(date2) > 0) return false;
            } else {
                if (date1.compareTo(date2) < 0) return false;
            }
        }
        return true;
    }

    @Test(description = "Verify Regularization Dates with Multiple Filters", groups = "P1")
    public void TC25_MultipleFilters1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("status", "PENDING");
        queryParams.put("reason", "SICK_LEAVE");
        queryParams.put("fromDate", "2024-01-01");
        queryParams.put("toDate", "2024-01-31");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertNotNull(jsonPath.get("data.regularizationDates"));
    }

    @Test(description = "Verify Regularization Dates Export Functionality", groups = "P1")
    public void TC26_ExportFunctionality1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("export", "true");
        queryParams.put("format", "CSV");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        Assert.assertTrue(response.contains("date,status,reason"));
        Assert.assertTrue(response.contains("\n"));
    }

    @Test(description = "Verify Regularization Dates with Bulk Employee Codes", groups = "P1")
    public void TC27_BulkEmployeeCodes1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCodes", newFSEEmpId + "," + newTLEmpId);

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newTLCustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        Assert.assertTrue(jsonPath.getList("data").size() > 1);
    }

    @Test(description = "Verify Regularization Dates API Rate Limiting", groups = "P1")
    public void TC28_RateLimiting1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);

        // Make multiple requests in quick succession
        for (int i = 0; i < 10; i++) {
            String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                    queryParams,
                    Headers.getHeadersAgent360(newFSECustId),
                    i < 5 ? 200 : 429);

            if (i >= 5) {
                JsonPath jsonPath = new JsonPath(response);
                Assert.assertTrue(jsonPath.getString("errorMessage").contains("Too many requests"));
            }
            Thread.sleep(100); // Small delay between requests
        }
    }

    @Test(description = "Verify Regularization Dates with Different Time Zones", groups = "P1")
    public void TC29_TimeZoneHandling1() throws Exception {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("timezone", "Asia/Kolkata");

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));
        // Verify the dates are adjusted according to timezone
        Assert.assertNotNull(jsonPath.get("data.timezone"));
    }

    @Test(description = "Verify Regularization Dates Data Archival", groups = "P1")
    public void TC30_DataArchival1() throws Exception {
        // First, verify very old data is archived
        String oldDate = "2020-01-01";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("empCode", newFSEEmpId);
        queryParams.put("date", oldDate);

        String response = request.getRequest(ApiEndPoint.Agent_360_Get_Regularization_Dates,
                queryParams,
                Headers.getHeadersAgent360(newFSECustId),
                200);

        JsonPath jsonPath = new JsonPath(response);
        Assert.assertTrue(jsonPath.getBoolean("success"));

        // Verify archived data in DB
        String query = String.format("SELECT * FROM attendance_regularization_archive WHERE emp_id = '%s' AND date = '%s'",
                newFSEEmpId, oldDate);
        ResultSet rs = getDataFromDBUtils.getRowDataFromDB(dbBase, query);
        Assert.assertTrue(rs.next(), "Archived data not found");
    }

}
