package org.wso2.lc.executor.sample.executors;

import org.wso2.lc.executor.sample.EmailExecutorException;
import org.wso2.lc.executor.sample.Constants.ExecutorConstants;
import org.wso2.lc.executor.sample.utils.APIGatewayUtils;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.util.Map;

public class EmailExecutor implements Execution {

    private static final Log log = LogFactory.getLog(EmailExecutor.class);

    private String emailListString = null;

    @Override
    public void init(Map parameterMap) {
        SecretResolver secretResolver = SecretResolverFactory.create((OMElement) null, false);
        // Retrieves the secured password as follows
        secretResolver.init(GovernanceRegistryExtensionsComponent.getSecretCallbackHandlerService()
                .getSecretCallbackHandler());
        if ((parameterMap.get(ExecutorConstants.EMAIL_LIST) != null) && (parameterMap.get(ExecutorConstants.EMAIL_LIST)
                != "")) {
            emailListString = parameterMap.get(ExecutorConstants.EMAIL_LIST).toString();
        }
        if (log.isDebugEnabled()) {
            log.debug(">>>>>>>>>>>>> EXECUTOR INIT <<<<<<<<<<<");
        }
    }

    /**
     * This method is used to execute the APIGateway executor.
     *
     * @param requestContext    request context.
     * @param currentState      current lifecycle state.
     * @param targetState       target state.
     * @return returns the executor status.
     */
    @Override
    public boolean execute(RequestContext requestContext, String currentState, String targetState) {

        if (log.isDebugEnabled()) {
            log.debug(">>>>>>>>>>>>> EXECUTOR PROCESS STARTED <<<<<<<<<<<");
        }
        Resource resource = requestContext.getResource();
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        //TODO: Need to check is it only the default LC
        String currentLifecycle = requestContext.getResource().getProperty("registry.LC.name");

        try {
            boolean status = APIGatewayUtils.sendMail(emailListString, targetState, currentState, currentLifecycle);
        } catch (EmailExecutorException e) {
            log.error("Error occurred while sending email.", e);
            return false;
        }

        return true;
    }
}


