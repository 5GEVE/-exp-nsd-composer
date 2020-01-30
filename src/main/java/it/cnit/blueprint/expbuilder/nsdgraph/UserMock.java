package it.cnit.blueprint.expbuilder.nsdgraph;

public class UserMock {

  //TODO in Test code set variable to know which test is being executed and return correct values

  public static VirtualLinkProfileVertex getVLPVertex1(NsdGraph exp) {
    return (VirtualLinkProfileVertex) exp.getG().vertexSet().stream()
        .filter(v -> v.getVertexId().equals("vl_profile_users")).findAny().get();
  }

  public static VirtualLinkProfileVertex getVLPVertex2(NsdGraph exp) {
    return (VirtualLinkProfileVertex) exp.getG().vertexSet().stream()
        .filter(v -> v.getVertexId().equals("vl_profile_origin")).findAny().get();
  }
  public static String getEdge1(NsdGraph exp){
    return "vCacheEdge_2_users_ext";
  }

  public static String getEdge2(NsdGraph exp){
    return "vCacheMid_origin_ext";
  }

  public static String getContextType(){
    return "passthrough";
    //return "normal";
  }

}
