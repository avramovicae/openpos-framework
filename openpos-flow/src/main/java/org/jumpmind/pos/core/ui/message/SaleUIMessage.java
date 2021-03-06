package org.jumpmind.pos.core.ui.message;

import java.util.ArrayList;
import java.util.List;

import org.jumpmind.pos.core.model.Total;
import org.jumpmind.pos.core.ui.ActionItem;
import org.jumpmind.pos.core.ui.AssignKeyBindings;
import org.jumpmind.pos.core.ui.UIMessage;
import org.jumpmind.pos.core.ui.data.AdditionalLabel;
import org.jumpmind.pos.core.ui.data.OrderSummary;

@AssignKeyBindings
public class SaleUIMessage extends UIMessage {
    private static final long serialVersionUID = 1L;

    private String providerKey;

    private List<Total> totals;
    private Total grandTotal;
    private Total itemCount;
    

    private List<OrderSummary> orders;
    private ActionItem removeOrderAction;

    private ActionItem checkoutButton;
    private ActionItem helpButton;
    private ActionItem logoutButton;
    private ActionItem loyaltyButton;
    private ActionItem mobileLoyaltyButton;
    private ActionItem linkedCustomerButton;
    private ActionItem promoButton;

    private boolean transactionActive = false;

    private UICustomer customer;
    private AdditionalLabel taxExemptCertificateDetail;

    private boolean locationEnabled;
    private String locationOverridePrompt;

    private boolean enableCollapsibleItems = true;

    public SaleUIMessage() {
        this.setScreenType(UIMessageType.SALE);
        this.setId("sale");
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public List<Total> getTotals() {
        return totals;
    }

    public void setTotals(List<Total> totals) {
        this.totals = totals;
    }

    public void addTotal(String name, String amount) {
        if (totals == null) {
            totals = new ArrayList<>();
        }
        totals.add(new Total(name, amount));
    }

    public Total getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Total grandTotal) {
        this.grandTotal = grandTotal;
    }

    public void setGrandTotal(String name, String amount) {
        this.grandTotal = new Total(name, amount);
    }

    public List<OrderSummary> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderSummary> orders) {
        this.orders = orders;
    }

    public void addOrder(OrderSummary orderSummary) {
        if(this.orders == null) {
            this.orders = new ArrayList<>();
        }
        this.orders.add(orderSummary);
    }

    public ActionItem getRemoveOrderAction() {
        return removeOrderAction;
    }

    public void setRemoveOrderAction(ActionItem removeOrderAction) {
        this.removeOrderAction = removeOrderAction;
    }

    public UICustomer getCustomer() {
        return customer;
    }

    public void setCustomer(UICustomer customer) {
        this.customer = customer;
    }

    public AdditionalLabel getTaxExemptCertificateDetail() {
        return taxExemptCertificateDetail;
    }

    public void setTaxExemptCertificateDetail(AdditionalLabel taxExemptCertificateDetail) {
        this.taxExemptCertificateDetail = taxExemptCertificateDetail;
    }

    public void setTaxExemptCertificateDetail(String label, String value) {
        this.taxExemptCertificateDetail = new AdditionalLabel(label, value);
    }

    public ActionItem getLoyaltyButton() {
        return loyaltyButton;
    }

    public void setLoyaltyButton(ActionItem loyaltyButton) {
        this.loyaltyButton = loyaltyButton;
    }

    public ActionItem getMobileLoyaltyButton() {
        return mobileLoyaltyButton;
    }

    public void setMobileLoyaltyButton(ActionItem mobileLoyaltyButton) {
        this.mobileLoyaltyButton = mobileLoyaltyButton;
    }

    public boolean isLocationEnabled() {
        return locationEnabled;
    }

    public void setLocationEnabled(boolean locationEnabled) {
        this.locationEnabled = locationEnabled;
    }

    public String getLocationOverridePrompt() {
        return locationOverridePrompt;
    }

    public void setLocationOverridePrompt(String locationOverridePrompt) {
        this.locationOverridePrompt = locationOverridePrompt;
    }

    public ActionItem getPromoButton() {
        return promoButton;
    }

    public void setPromoButton(ActionItem promoButton) {
        this.promoButton = promoButton;
    }
    
    public void setTransactionActive(boolean isTransactionActive) {
        this.transactionActive = isTransactionActive;
    }

    public boolean isTransactionActive() {
        return transactionActive;
    }

    public Total getItemCount() {
        return itemCount;
    }

    public void setItemCount(String name, String amount) {
        this.setItemCount(new Total(name, amount));
    }
    
    public void setItemCount(Total itemCount) {
        this.itemCount = itemCount;
    }

    public ActionItem getCheckoutButton() {
        return checkoutButton;
    }

    public void setCheckoutButton(ActionItem checkoutButton) {
        this.checkoutButton = checkoutButton;
    }

    public ActionItem getLogoutButton() {
        return logoutButton;
    }

    public void setLogoutButton(ActionItem logoutButton) {
        this.logoutButton = logoutButton;
    }

    public ActionItem getHelpButton() {
        return helpButton;
    }

    public void setHelpButton(ActionItem helpButton) {
        this.helpButton = helpButton;
    }

    public boolean isEnableCollapsibleItems() {
        return enableCollapsibleItems;
    }

    public void setEnableCollapsibleItems(boolean enableCollapsibleItems) {
        this.enableCollapsibleItems = enableCollapsibleItems;
    }

    public ActionItem getLinkedCustomerButton() {
        return linkedCustomerButton;
    }

    public void setLinkedCustomerButton(ActionItem linkedCustomerButton) {
        this.linkedCustomerButton = linkedCustomerButton;
    }
}
