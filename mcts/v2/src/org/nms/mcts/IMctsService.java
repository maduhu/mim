/**
 * IMctsService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.nms.mcts;

public interface IMctsService extends java.rmi.Remote {
    public org.nms.mcts.DS_GetChildDataResponseDS_GetChildDataResult DS_GetChildData(java.lang.String id, java.lang.String pwd, java.lang.String fdate, java.lang.String tdate, java.lang.String sid) throws java.rmi.RemoteException;
    public org.nms.mcts.DS_GetMotherDataResponseDS_GetMotherDataResult DS_GetMotherData(java.lang.String id, java.lang.String pwd, java.lang.String fdate, java.lang.String tdate, java.lang.String sid) throws java.rmi.RemoteException;
    public org.nms.mcts.DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult DS_GetAnmAshaData(java.lang.String id, java.lang.String pwd, java.lang.String fdate, java.lang.String tdate, java.lang.String sid) throws java.rmi.RemoteException;
}
