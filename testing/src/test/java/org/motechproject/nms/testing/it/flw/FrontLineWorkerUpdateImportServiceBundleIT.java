package org.motechproject.nms.testing.it.flw;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.repository.CsvAuditRecordDataService;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.service.FrontLineWorkerUpdateImportService;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
import org.motechproject.nms.testing.it.utils.RegionHelper;
import org.motechproject.nms.testing.service.TestingService;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class FrontLineWorkerUpdateImportServiceBundleIT extends BasePaxIT {

    @Inject
    CircleDataService circleDataService;
    @Inject
    DistrictDataService districtDataService;
    @Inject
    DistrictService districtService;
    @Inject
    StateDataService stateDataService;
    @Inject
    LanguageDataService languageDataService;
    @Inject
    FrontLineWorkerDataService frontLineWorkerDataService;
    @Inject
    FrontLineWorkerService frontLineWorkerService;
    @Inject
    TestingService testingService;
    @Inject
    FrontLineWorkerUpdateImportService frontLineWorkerUpdateImportService;
    @Inject
    CsvAuditRecordDataService csvAuditRecordDataService;


    private RegionHelper rh;
    private String resource;


    @Before
    public void setUp() {
        rh = new RegionHelper(languageDataService, circleDataService, stateDataService, districtDataService,
                districtService);

        testingService.clearDatabase();

        rh.hindiLanguage();
        rh.kannadaLanguage();
        rh.delhiState();
        rh.newDelhiDistrict();
    }

    // Test when state not provided
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenStateNotPresent() throws Exception {
        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,9439986187,en,");
        frontLineWorkerUpdateImportService.importLanguageData(reader);
    }

    // Test when state not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenStateNotInDatabase() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(9439986187L);
        frontLineWorkerService.add(flw);

        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,9439986187,en,2");
        frontLineWorkerUpdateImportService.importLanguageData(reader);
    }

    // Test when language not provided
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageNotPresent() throws Exception {
        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,9439986187,,1");
        frontLineWorkerUpdateImportService.importLanguageData(reader);
    }

    // Test when language not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenLanguageNotInDatabase() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(9439986187L);
        frontLineWorkerService.add(flw);

        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,9439986187,en,1");
        frontLineWorkerUpdateImportService.importLanguageData(reader);
    }

    // Test when only NMS Id found and FLW not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenFLWIdProvidedButNotInDatabase() throws Exception {
        Reader reader = createLanguageReaderWithHeaders("72185,,,hi,1");
        frontLineWorkerUpdateImportService.importLanguageData(reader);
    }

    // NMS_FT_553
    // Test when only MCTS Id found and FLW not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenMCTSIdProvidedButNotInDatabase() throws Exception {
        Reader reader = createLanguageReaderWithHeaders(",210302604211400029,,hi,1");
        frontLineWorkerUpdateImportService.importLanguageData(reader);
    }

    // NMS_FT_554
    // Test when only MSISDN found and FLW not in database
    @Test(expected = CsvImportDataException.class)
    public void testImportWhenMSISDProvidedButNotInDatabase() throws Exception {
        Reader reader = createLanguageReaderWithHeaders(",,9439986187,hi,1");
        frontLineWorkerUpdateImportService.importLanguageData(reader);
    }

    // Test NMS Id takes precedence over MCTS ID
    @Test
    public void testImportWhenNMSIdTakesPrecedenceOverMCTSId() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setMctsFlwId("210302604211400029");
        flw.setLanguage(rh.kannadaLanguage());
        flw.setState(rh.delhiState());
        flw.setDistrict(rh.newDelhiDistrict());
        frontLineWorkerService.add(flw);

        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,,hi,1");
        frontLineWorkerUpdateImportService.importLanguageData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertEquals(rh.kannadaLanguage(), flw.getLanguage());
    }

    // Test NMS Id takes precedence over MSISDN
    @Test
    public void testImportWhenNMSIdTakesPrecedenceOverMSIDN() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        Reader reader = createLanguageReaderWithHeaders("72185,,2000000000,hi,1");
        frontLineWorkerUpdateImportService.importLanguageData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertEquals(rh.kannadaLanguage(), flw.getLanguage());
    }

    // Test MCTS Id takes precedence over MSISDN
    @Test
    public void testImportWhenMCTSIdTakesPrecedenceOverMSIDN() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setMctsFlwId("210302604211400029");
        flw.setLanguage(rh.kannadaLanguage());
        flw.setState(rh.delhiState());
        flw.setDistrict(rh.newDelhiDistrict());
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,2000000000,hi,1");
        frontLineWorkerUpdateImportService.importLanguageData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertEquals(rh.kannadaLanguage(), flw.getLanguage());
    }

    // Test MSISDN only
    @Test
    public void testImportWhenMSISDNOnly() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        Reader reader = createLanguageReaderWithHeaders("72185,210302604211400029,1000000000,hi,1");
        frontLineWorkerUpdateImportService.importLanguageData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertEquals(rh.kannadaLanguage(), flw.getLanguage());
    }

    @Test
    public void testImportFromSampleLanguageDataFile() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        flw.setFlwId("72185");
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setLanguage(rh.kannadaLanguage());
        frontLineWorkerService.add(flw);

        frontLineWorkerUpdateImportService.importLanguageData(read("csv/flw_language_update.csv"));

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertEquals(rh.hindiLanguage(), flw.getLanguage());
    }

    /************************************************************************************************************
     MSISDN TESTS
     ***********************************************************************************************************/
    // Test when new msisdn not provided
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenNewMsisdnNotPresent() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders("72185,210302604211400029,9439986187,,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);
    }

    // Test when only NMS Id found and FLW not in database
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenFLWIdProvidedButNotInDatabase() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders("72185,,,9439986187,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);
    }

    // Test when only MCTS Id found and FLW not in database
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenMCTSIdProvidedButNotInDatabase() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders(",210302604211400029,,9439986187,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);
    }

    // Test when only MSISDN found and FLW not in database
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenMSISDNProvidedButNotInDatabase() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders(",,9439986187,9439986188,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);
    }

    // Test NMS Id takes precedence over MCTS ID
    @Test
    public void testMsisdnImportWhenNMSIdTakesPrecedenceOverMCTSId() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        flw.setMctsFlwId("210302604211400029");
        flw.setState(rh.delhiState());
        flw.setDistrict(rh.newDelhiDistrict());
        frontLineWorkerService.add(flw);

        Reader reader = createMSISDNReaderWithHeaders("72185,210302604211400029,,9439986187,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(9439986187L);
        assertNotNull(flw);
        assertEquals("72185", flw.getFlwId());

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertNull(flw);

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertNotNull(flw);
    }

    // Test NMS Id takes precedence over MSISDN
    @Test
    public void testMsisdnImportWhenNMSIdTakesPrecedenceOverMSIDN() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        frontLineWorkerService.add(flw);

        Reader reader = createMSISDNReaderWithHeaders("72185,,2000000000,9439986187,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(9439986187L);
        assertNotNull(flw);
        assertEquals("72185", flw.getFlwId());

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertNull(flw);

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertNotNull(flw);
    }

    // Test MCTS Id takes precedence over MSISDN
    @Test
    public void testMsisdnImportWhenMCTSIdTakesPrecedenceOverMSIDN() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setMctsFlwId("210302604211400029");
        flw.setState(rh.delhiState());
        flw.setDistrict(rh.newDelhiDistrict());
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        frontLineWorkerService.add(flw);

        Reader reader = createMSISDNReaderWithHeaders("72185,210302604211400029,2000000000,9439986187,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(9439986187L);
        assertNotNull(flw);
        assertEquals("210302604211400029", flw.getMctsFlwId());

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertNull(flw);

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertNotNull(flw);
    }

    // Test MSISDN only
    @Test
    public void testMsisdnImportWhenMSISDNOnly() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        frontLineWorkerService.add(flw);

        Reader reader = createMSISDNReaderWithHeaders("72185,210302604211400029,1000000000,9439986187,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(9439986187L);
        assertNotNull(flw);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertNull(flw);

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertNotNull(flw);
    }

    @Test
    public void testMsisdnImportFromSampleDataFile() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(2000000000L);
        frontLineWorkerService.add(flw);

        frontLineWorkerUpdateImportService.importMSISDNData(read("csv/flw_msisdn_update.csv"));

        flw = frontLineWorkerDataService.findByContactNumber(9439986187L);
        assertNotNull(flw);

        flw = frontLineWorkerDataService.findByContactNumber(9439986188L);
        assertNotNull(flw);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertNull(flw);

        flw = frontLineWorkerDataService.findByContactNumber(2000000000L);
        assertNull(flw);
    }

    // Test new MSISDN larger than 10 digits
    @Test
    public void testMsisdnImportWhenNewMsisdnTooLong() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        frontLineWorkerService.add(flw);

        Reader reader = createMSISDNReaderWithHeaders("72185,210302604211400029,1000000000,09439986187,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);

        flw = frontLineWorkerDataService.findByContactNumber(9439986187L);
        assertNotNull(flw);

        flw = frontLineWorkerDataService.findByContactNumber(1000000000L);
        assertNull(flw);
    }

    // NMS_FT_557
    // Test new MSISDN not a valid number
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenMSISDNProvidedButNotValid() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders(",,9439986187,AAAAAAAAAA,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);
    }

    // NMS_FT_557
    // Test new MSISDN less than 10 digits
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenMSISDNProvidedButTooShort() throws Exception {
        Reader reader = createMSISDNReaderWithHeaders(",,9439986187,943998618,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);
    }

    // NMS_FT_556
    // Test new MSISDN associated with existing FLW
    @Test(expected = CsvImportDataException.class)
    public void testMsisdnImportWhenMSISDNProvidedButAlreadyInUse() throws Exception {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        frontLineWorkerService.add(flw);

        flw = new FrontLineWorker(9439986187L);
        frontLineWorkerService.add(flw);

        Reader reader = createMSISDNReaderWithHeaders(",,9439986187,1000000000,1");
        frontLineWorkerUpdateImportService.importMSISDNData(reader);
    }

    private Reader createMSISDNReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("NMS FLW-ID,MCTS FLW-ID,MSISDN,NEW MSISDN,STATE").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader createLanguageReaderWithHeaders(String... lines) {
        StringBuilder builder = new StringBuilder();
        builder.append("NMS FLW-ID,MCTS FLW-ID,MSISDN,LANGUAGE CODE,STATE").append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return new StringReader(builder.toString());
    }

    private Reader read(String resource) {
        return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
    }

    /**
     * Method used to import CSV File For updating FLW Data. option can be
     * "msisdn" or "language"
     */
    private HttpResponse importCsvFileForFLWUpdate(String option,
            String fileName)
            throws InterruptedException, IOException {
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/flw/update/%s",
                TestContext.getJettyPort(), option));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart(
                "csvFile",
                new FileBody(new File(String.format(
                        "src/test/resources/csv/%s", fileName))));
        httpPost.setEntity(builder.build());

        HttpResponse response = SimpleHttpClient.httpRequestAndResponse(
                httpPost, RequestBuilder.ADMIN_USERNAME,
                RequestBuilder.ADMIN_PASSWORD);
        return response;
    }

    /**
     * To verify language is updated successfully when MCTS FLW ID is provided.
     */
    @Test
    public void verifyFT550() throws InterruptedException, IOException {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        flw.setMctsFlwId("210302604211400029");
        flw.setLanguage(rh.kannadaLanguage());
        flw.setState(rh.delhiState());
        flw.setDistrict(rh.newDelhiDistrict());
        frontLineWorkerService.add(flw);

        assertEquals(
                HttpStatus.SC_OK,
                importCsvFileForFLWUpdate("language",
                        "flw_language_update_only_flwId.csv").getStatusLine()
                        .getStatusCode());

        flw = frontLineWorkerService.getByFlwId("72185");
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        assertEquals(1, csvAuditRecordDataService.count());
        assertEquals("Success", csvAuditRecordDataService.retrieveAll().get(0)
                .getOutcome());
    }

    /**
     * To verify language is updated successfully when MSISDN is provided.
     */
    @Test
    public void verifyFT551() throws InterruptedException, IOException {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        flw.setMctsFlwId("210302604211400029");
        flw.setLanguage(rh.kannadaLanguage());
        flw.setState(rh.delhiState());
        flw.setDistrict(rh.newDelhiDistrict());
        frontLineWorkerService.add(flw);

        assertEquals(
                HttpStatus.SC_OK,
                importCsvFileForFLWUpdate("language",
                        "flw_language_update_only_MSISDN.csv").getStatusLine()
                        .getStatusCode());

        flw = frontLineWorkerService.getByFlwId("72185");
        assertEquals(rh.hindiLanguage(), flw.getLanguage());

        assertEquals(1, csvAuditRecordDataService.count());
        assertEquals("Success", csvAuditRecordDataService.retrieveAll().get(0)
                .getOutcome());
    }

    /**
     * To verify language updated is getting rejected when language provided is
     * having invalid value.
     */
    // TODO JIRA issue https://applab.atlassian.net/browse/NMS-252
    @Test
    public void verifyFT552() throws InterruptedException, IOException {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        flw.setMctsFlwId("210302604211400029");
        flw.setLanguage(rh.kannadaLanguage());
        flw.setState(rh.delhiState());
        flw.setDistrict(rh.newDelhiDistrict());
        frontLineWorkerService.add(flw);

        assertEquals(
                HttpStatus.SC_BAD_REQUEST,
                importCsvFileForFLWUpdate("language",
                        "flw_language_update_lang_error.csv").getStatusLine()
                        .getStatusCode());

        flw = frontLineWorkerService.getByFlwId("72185");
        assertEquals(rh.kannadaLanguage(), flw.getLanguage());

        assertEquals(1, csvAuditRecordDataService.count());
        assertTrue(csvAuditRecordDataService.retrieveAll().get(0).getOutcome()
                .contains("Failure"));
    }

    /**
     * To verify MSISDN is updated successfully when MCTS FLW ID is provided.
     */
    @Test
    public void verifyFT555() throws InterruptedException, IOException {
        FrontLineWorker flw = new FrontLineWorker(1000000000L);
        flw.setFlwId("72185");
        flw.setMctsFlwId("210302604211400029");
        flw.setLanguage(rh.kannadaLanguage());
        flw.setState(rh.delhiState());
        flw.setDistrict(rh.newDelhiDistrict());
        frontLineWorkerService.add(flw);

        assertEquals(
                HttpStatus.SC_OK,
                importCsvFileForFLWUpdate("msisdn",
                        "flw_msisdn_update_only_flwId.csv").getStatusLine()
                        .getStatusCode());

        flw = frontLineWorkerService.getByContactNumber(9439986187L);
        assertNotNull(flw);

        flw = frontLineWorkerService.getByContactNumber(1000000000L);
        assertNull(flw);

        assertEquals(1, csvAuditRecordDataService.count());
        assertEquals("Success", csvAuditRecordDataService.retrieveAll().get(0)
                .getOutcome());
    }
}