package org.motechproject.nms.mcts;

import org.apache.axis.message.MessageElement;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.handler.HandlerRegistry;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Iterator;

/**
 * MCTS client to sync data
 */
public class MctsClient {

    public static void main(String[] args) {

        try {
            MctsServiceLocator serviceLocator = new MctsServiceLocator();
            IMctsService dataService = serviceLocator.getbasicEndpoint();

            System.out.println("Hello World");
            System.out.println(serviceLocator.getbasicEndpointAddress());
            DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult result =
                    dataService.DS_GetAnmAshaData("mcts-Orissa", "OMyhYekc5H2f+0dAOiHVKBh5m4FTdys68LKG0+dqK/c=", "20-08-2015", "24-08-2015", "21");
            DS_GetChildDataResponseDS_GetChildDataResult childDataResult = dataService.DS_GetChildData("mcts-Orissa", "OMyhYekc5H2f+0dAOiHVKBh5m4FTdys68LKG0+dqK/c=", "20-08-2015", "24-08-2015", "21");
            for (MessageElement me : result.get_any()) {
                System.out.println(me.toString());
            }

            for (MessageElement me : childDataResult.get_any()) {
                System.out.println(me.toString());
            }

        } catch (RemoteException re) {
            System.out.println(re.toString());
        } catch (ServiceException se) {
            System.out.println(se.toString());
        }


    }
}
