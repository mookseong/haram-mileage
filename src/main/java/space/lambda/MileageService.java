package space.lambda;

import java.io.IOException;
import java.util.ArrayList;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import space.lambda.api.MileageApi;
import space.lambda.data.Detail;
import space.lambda.model.MileageModel;
import space.lambda.model.Type;
import space.lambda.data.User;
import space.lambda.util.LoggerUtil;

public class MileageService {

  public static final OkHttpClient client = new OkHttpClient().newBuilder().build();
  private final LoggerUtil logger = new LoggerUtil();

  public Response mileageRequest(
      MileageApi mileageApi,
      MileageModel event,
      String cookie
  ) throws IOException {
    logger.writeLogger("An API request was made to the mileage server." + event.toType());
    RequestBody body = mileageApi.setBody(event);
    Request request = mileageApi.getRequest(body, cookie);
    return client.newCall(request).execute();
  }

  public String mileageLogin(MileageFactory factory, MileageModel event) {
    logger.writeLogger("No login information, try logging in");
    MileageApi mileageApi = factory.getMileage(Type.LOGIN);
    try (Response response = mileageRequest(mileageApi, event, "")) {
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      }
      String cookie = response.header("Set-Cookie");
      logger.writeLogger("set cookie : " + cookie);
      return cookie;
    } catch (IOException e) {
      logger.writeLogger("An error occurred during the login process" + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public ArrayList<Detail> mileageFindUser(NodeList nList) {
    ArrayList<Detail> details = new ArrayList<>(nList.getLength());
    for (int i = 0; i < nList.getLength(); i++) {
      Node nNode = nList.item(i);
      if (nNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element eElement = (Element) nNode;
      NodeList nList2 = eElement.getElementsByTagName("TD");
      Detail user = Detail.builder()
          .changeDate(getTextContent(nList2, 0))
          .saleDate(getTextContent(nList2, 1))
          .status(getTextContent(nList2, 2))
          .point(getTextContent(nList2, 3))
          .note(getTextContent(nList2, 4))
          .build();
      details.add(user);
    }  // if end
    return details;
  }

  public ArrayList<User> mileageAllUser(NodeList nList) {
    ArrayList<User> users = new ArrayList<>(nList.getLength());
    for (int i = 0; i < nList.getLength(); i++) {
      Node nNode = nList.item(i);
      if (nNode.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      Element eElement = (Element) nNode;
      NodeList nList2 = eElement.getElementsByTagName("TD");
      User user = User.builder()
          .uniqueNumber(getTextContent(nList2, 0))
          .userType(getTextContent(nList2, 1))
          .studentStatus(getTextContent(nList2, 2))
          .cardNumber(getTextContent(nList2, 3))
          .earnPoints(getTextContent(nList2, 4))
          .usePoints(getTextContent(nList2, 5))
          .adjustmentPoint(getTextContent(nList2, 6))
          .availablePoints(getTextContent(nList2, 7))
          .paymentsNum(getTextContent(nList2, 8))
          .amountPayment(getTextContent(nList2, 9))
          .joinDate(getTextContent(nList2, 10))
          .build();
      users.add(user);
    }  // if end
    return users;
  }

  private static String getTextContent(NodeList nodeList, int index) {
    if (nodeList.item(index) != null) {
      return nodeList.item(index).getTextContent().trim();
    }
    return "";
  }

}
