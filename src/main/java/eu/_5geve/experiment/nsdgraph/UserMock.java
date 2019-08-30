package eu._5geve.experiment.nsdgraph;

public class UserMock {

  public static VirtualLinkProfileVertex getVLPVertex1(NsdGraph exp) {
    return (VirtualLinkProfileVertex) exp.getG().vertexSet().stream()
        .filter(v -> v.getProfileId().equals("vl_profile_users")).findAny().get();
  }

  public static VirtualLinkProfileVertex getVLPVertex2(NsdGraph exp) {
    return (VirtualLinkProfileVertex) exp.getG().vertexSet().stream()
        .filter(v -> v.getProfileId().equals("vl_profile_origin")).findAny().get();
  }
  public static String getEdge(NsdGraph exp){
    return "vCacheEdge_2_users_ext";
  }

  public static String getContextType(){
    return "passthrough";
    //return "normal";
  }

}
