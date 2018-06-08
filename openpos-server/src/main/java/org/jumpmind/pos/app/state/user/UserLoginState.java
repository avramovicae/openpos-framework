package org.jumpmind.pos.app.state.user;

import org.apache.commons.collections.CollectionUtils;
import org.jumpmind.pos.app.state.AbstractState;
import org.jumpmind.pos.context.service.ContextServiceClient;
import org.jumpmind.pos.core.flow.Action;
import org.jumpmind.pos.core.flow.ActionHandler;
import org.jumpmind.pos.core.flow.IState;
import org.jumpmind.pos.core.flow.In;
import org.jumpmind.pos.core.flow.InOut;
import org.jumpmind.pos.core.flow.ScopeType;
import org.jumpmind.pos.core.flow.ui.PromptConfig;
import org.jumpmind.pos.core.screen.IPromptScreen;
import org.jumpmind.pos.service.ServiceResult;
import org.jumpmind.pos.user.model.User;
import org.jumpmind.pos.user.service.AuthenticationResult;
import org.jumpmind.pos.user.service.UserMessage;
import org.jumpmind.pos.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class UserLoginState extends AbstractState {

    @Autowired
    private UserService userService;

    @InOut(scope = ScopeType.Session)
    protected User currentUser;

    @In(scope = ScopeType.Node)
    private ContextServiceClient contextServiceClient;

    private String enteredUserName;
    private int userMessageIndex = 0;
    private AuthenticationResult result;
    private String oldPassword;
    private String newPassword1;

    private IState sourceState;
    private IState targetState;

    public UserLoginState(IState sourceState, IState targetState) {
        this.sourceState = sourceState;
        this.targetState = targetState;
    }

    @Override
    public void arrive(Action action) {
        String welcomeText = contextServiceClient.getString("pos.welcome.text"); // testing.
        System.out.println("Welcome - " + welcomeText);
        promptForLogin();
    }

    @Override
    protected String getDefaultBundleName() {
        return "user";
    }

    protected void promptForLogin() {
        stateManager.getUI().prompt(new PromptConfig().placeholder(resource("_loginUserId")).promptText(resource("_loginUserPromptText"))
                .icon("lock").action(commonResource("_nextButton"), "UsernameEntered"));
    }

    protected void promptForPassword() {
        stateManager.getUI()
                .prompt(new PromptConfig().placeholder(resource("_loginPassword")).promptText(resource("_loginPasswordPrompt"))
                        .promptType(IPromptScreen.TYPE_ALPHANUMERICPASSWORD).icon("lock")
                        // .addOtherAction(new MenuItem("ForgotPassword",
                        // "Forgot Password"))
                        .action(commonResource("_nextButton"), "PasswordEntered").backAction("BackToUserPrompt"));

    }

    protected void processResult() {
        if (isResultSuccessful()) {
            onSuccessfulAuthentication();
            stateManager.transitionTo(null, targetState);
        } else {
            if (CollectionUtils.isEmpty(result.getUserMessages())) {
                stateManager.getUI().notify(result.getResultMessage(), "FailureAcknowledged");
            }
        }
    }

    protected boolean isResultSuccessful() {
        return result.getResultStatus().equals("SUCCESS");
    }

    protected User getResultUser() {
        return result.getUser();
    }

    protected void onSuccessfulAuthentication() {
        this.currentUser = result.getUser();
    }

    protected void showUserMessages() {
        if (CollectionUtils.isEmpty(result.getUserMessages()) || userMessageIndex >= result.getUserMessages().size()) {
            processResult();
            return;
        }

        UserMessage userMessage = result.getUserMessages().get(userMessageIndex++);
        switch (userMessage.getMessageCode()) {
            case "PASSWORD_EXPIRED":
            case "PASSWORD_EXPIRY_WARNING":
                stateManager.getUI().askYesNo(userMessage.getMessage() + " Would you like to change your password now?", "ChangePasswordYes",
                        "ChangePasswordNo");
                break;
        }
    }

    @ActionHandler
    public void onUsernameEntered(Action action) {
        enteredUserName = (String) action.getData();
        promptForPassword();
    }

    @ActionHandler
    public void onPasswordEntered(Action action) {
        String password = (String) action.getData();
        oldPassword = password;
        result = userService.authenticate(stateManager.getNodeId(), null, enteredUserName, password);
        userMessageIndex = 0;
        showUserMessages();
    }

    @ActionHandler
    public void onBackToUserPrompt(Action action) {
        promptForLogin();
    }

    @ActionHandler
    public void onBack(Action action) {
        stateManager.transitionTo(null, sourceState);
    }

    @ActionHandler
    public void onFailureAcknowledged(Action action) {
        promptForLogin();
    }

    @ActionHandler
    public void onForgotPassword(Action action) {
        promptForLogin();
    }

    @ActionHandler
    public void onChangePasswordYes(Action action) {
        stateManager.getUI()
                .prompt(new PromptConfig().placeholder("New Password").promptText("Type your New Password and press enter.")
                        .promptType(IPromptScreen.TYPE_ALPHANUMERICPASSWORD).icon("lock").action("Next", "NewPassword1Entered")
                        .backAction("BackToUserPrompt"));
    }

    @ActionHandler
    public void onNewPassword1Entered(Action action) {
        newPassword1 = (String) action.getData();
        stateManager.getUI()
                .prompt(new PromptConfig().placeholder("New Password Again").promptText("Type your New Password again and press enter.")
                        .promptType(IPromptScreen.TYPE_ALPHANUMERICPASSWORD).icon("lock").action("Next", "NewPassword2Entered")
                        .backAction("BackToUserPrompt"));
    }

    @ActionHandler
    public void onNewPassword2Entered(Action action) {
        String newPassword2 = (String) action.getData();

        ServiceResult changePasswordResult = userService.changePassword(stateManager.getNodeId(), null, result.getUser().getUsername(),
                oldPassword, newPassword1, newPassword2);
        if (changePasswordResult.getResultStatus().equals("SUCCESS")) {
            stateManager.getUI().notify("Your password was changed succesful.", "PasswordChangeAcknowledged");
        } else {
            stateManager.getUI().notify(changePasswordResult.getResultMessage(), "ChangePasswordYes");
        }

    }

    @ActionHandler
    public void onPasswordChangeAcknowledged(Action action) {
        onSuccessfulAuthentication();
        stateManager.transitionTo(null, targetState);
    }

    @ActionHandler
    public void onChangePasswordNo(Action action) {
        if (isResultSuccessful()) {
            processResult();
        } else {
            promptForLogin();
        }
    }

    @ActionHandler
    public void onMessageAcknowledged(Action action) {
        showUserMessages();
    }
}