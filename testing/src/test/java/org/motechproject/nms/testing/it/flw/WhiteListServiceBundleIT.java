package org.motechproject.nms.testing.it.flw;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.mds.dto.EntityDto;
import org.motechproject.mds.service.EntityService;
import org.motechproject.nms.flw.domain.WhitelistEntry;
import org.motechproject.nms.flw.domain.WhitelistState;
import org.motechproject.nms.flw.repository.ServiceUsageCapDataService;
import org.motechproject.nms.flw.repository.WhitelistEntryDataService;
import org.motechproject.nms.flw.repository.WhitelistStateDataService;
import org.motechproject.nms.flw.service.WhitelistService;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.testing.it.api.utils.RequestBuilder;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class WhiteListServiceBundleIT extends BasePaxIT {
    public static final Long WHITELIST_CONTACT_NUMBER = 1111111111l;
    public static final Long NOT_WHITELIST_CONTACT_NUMBER = 9000000000l;

    private State whitelistState;
    private State nonWhitelistState;

    @Inject
    ServiceUsageCapDataService serviceUsageCapDataService;

    @Inject
    WhitelistEntryDataService whitelistEntryDataService;

    @Inject
    WhitelistStateDataService whitelistStateDataService;

    @Inject
    StateDataService stateDataService;

    @Inject
    WhitelistService whitelistService;

    @Inject
    EntityService entityService;

    @Inject
    TestingService testingService;

    private void setupData() {
        testingService.clearDatabase();

        whitelistEntryDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        whitelistStateDataService.deleteAll();
        stateDataService.deleteAll();

        whitelistState = new State("New Jersey", 1l);
        stateDataService.create(whitelistState);

        whitelistStateDataService.create(new WhitelistState(whitelistState));

        nonWhitelistState = new State("Washington", 2l);
        stateDataService.create(nonWhitelistState);

        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER, whitelistState);
        whitelistEntryDataService.create(entry);
    }

    // Test with record in whitelist entry for a state that isn't in whitelist table
    // In this test the whitelist isn't turned on for that state so the call should be allowed
    @Test
    public void testRecordInWhitelistEntryButStateNotWhitelisted() throws Exception {
        setupData();

        WhitelistEntry entry = new WhitelistEntry(NOT_WHITELIST_CONTACT_NUMBER, nonWhitelistState);
        whitelistEntryDataService.create(entry);

        boolean result = whitelistService.numberWhitelistedForState(nonWhitelistState, NOT_WHITELIST_CONTACT_NUMBER);
        assertTrue(result);
    }

    // Test with null state
    @Test
    public void testNullState() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(null, WHITELIST_CONTACT_NUMBER);
        assertTrue(result);
    }

    // Test with null contactNumber
    @Test
    public void testNullContactNumber() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(whitelistState, null);
        assertTrue(result);
    }

    // Test both state and contactNumber null
    @Test
    public void testNullStateAndContactNumber() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(null, null);
        assertTrue(result);
    }

    // Test for state without whitelist enabled
    @Test
    public void testStateWithoutWhitelist() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(nonWhitelistState, WHITELIST_CONTACT_NUMBER);
        assertTrue(result);
    }

    // Test state with whitelist, number in list
    @Test
    public void testStateWithWhitelistValidNumber() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(whitelistState, WHITELIST_CONTACT_NUMBER);
        assertTrue(result);
    }

    // Test state with whitelist, number not in list
    @Test
    public void testStateWithWhitelistInvalidNumber() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(whitelistState, NOT_WHITELIST_CONTACT_NUMBER);
        assertFalse(result);
    }

    // Test with non-whitelist state, number not whitelisted
    @Test
    public void testStateWithoutWhitelistInvalidNumber() throws Exception {
        setupData();

        boolean result = whitelistService.numberWhitelistedForState(nonWhitelistState, NOT_WHITELIST_CONTACT_NUMBER);
        assertTrue(result);
    }

    // Test with non-whitelist state + whitelist number and non-whitelist state + non-whitelist number
    @Test
    public void testNumberInWhitelistForStateButWhitelistNotOnForState() throws Exception {
        whitelistEntryDataService.deleteAll();
        serviceUsageCapDataService.deleteAll();
        whitelistStateDataService.deleteAll();
        stateDataService.deleteAll();

        whitelistState = new State("New Jersey", 1l);
        stateDataService.create(whitelistState);

        whitelistStateDataService.create(new WhitelistState(whitelistState));

        nonWhitelistState = new State("Washington", 2l);
        stateDataService.create(nonWhitelistState);

        WhitelistEntry entry = new WhitelistEntry(WHITELIST_CONTACT_NUMBER, nonWhitelistState);
        whitelistEntryDataService.create(entry);

        boolean result = whitelistService.numberWhitelistedForState(nonWhitelistState, WHITELIST_CONTACT_NUMBER);
        assertTrue(result);

        result = whitelistService.numberWhitelistedForState(nonWhitelistState, NOT_WHITELIST_CONTACT_NUMBER);
        assertTrue(result);
    }

    @Test
    public void test() throws InterruptedException, IOException {
        EntityDto dto = entityService.getEntityByClassName("org.motechproject.nms.flw.domain.WhitelistEntry");
        Long entityID = dto.getId();
        importCsvFileForLocationData(entityID, "");
    }

    private void importCsvFileForLocationData(Long entityId,
                                                      String fileName)
            throws InterruptedException, IOException {
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/mds/instances/%s/csvimport",
                TestContext.getJettyPort(), entityId));
        FileBody fileBody = new FileBody(new File("/home/ashish/mim/mim/testing/src/test/resources/csv/whitelist.csv"));
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("csvFile", fileBody);
        httpPost.setEntity(builder.build());
        SimpleHttpClient.execHttpRequest(httpPost);
    }
}
