package org.wso2.lc.executor.sample.utils;

import org.wso2.lc.executor.sample.EmailExecutorException;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.eventing.internal.EventingDataHolder;

public class APIGatewayUtils {

    private static final Log log = LogFactory.getLog(APIGatewayUtils.class);

    /**
     * This method is used to send emails in the lifecycle approval and state change process.
     *
     * @param targetState           target state.
     * @param currentState          current state.
     * @param currentLifecycle      lifecycle name.
     * @return returns the email sending status.
     * @throws org.wso2.lc.executor.sample.EmailExecutorException
     */
    public static boolean sendMail(String emailListWithComma, String targetState, String currentState,
            String currentLifecycle) throws EmailExecutorException {

        ConfigurationContext configContext = EventingDataHolder.getInstance().getConfigurationContext();

        String[] emailList = emailListWithComma.split(",");
        for (String email : emailList) {
            EmailTransformUtils
                    .sendMimeMessage(configContext, "LC state change from " + currentState + " to " + targetState,
                            currentLifecycle + " LC.", email);
        }

        return true;
    }
}
