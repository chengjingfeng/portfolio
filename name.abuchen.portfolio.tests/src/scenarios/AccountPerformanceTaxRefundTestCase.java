package scenarios;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import name.abuchen.portfolio.model.Account;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.ClientFactory;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.snapshot.PerformanceIndex;
import name.abuchen.portfolio.snapshot.ReportingPeriod;
import name.abuchen.portfolio.util.Dates;

import org.junit.Test;

@SuppressWarnings("nls")
public class AccountPerformanceTaxRefundTestCase
{
    /**
     * Feature: when calculating the performance of an account, do include taxes
     * and tax refunds but only those that are not paid for a security.
     */
    @Test
    public void testAccountPerformanceTaxRefund() throws IOException
    {
        Client client = ClientFactory.load(SecurityTestCase.class
                        .getResourceAsStream("account_performance_tax_refund.xml"));

        Account account = client.getAccounts().get(0);
        ReportingPeriod period = new ReportingPeriod.FromXtoY(Dates.date("2013-12-06"), Dates.date("2014-12-06"));

        AccountTransaction deposit = account.getTransactions().get(0);

        // no changes in holdings, ttwror must be:
        double startValue = deposit.getAmount();
        double endValue = account.getCurrentAmount();
        double ttwror = (endValue / startValue) - 1;

        List<Exception> warnings = new ArrayList<Exception>();
        PerformanceIndex accountPerformance = PerformanceIndex.forAccount(client, account, period, warnings);
        assertThat(warnings, empty());
        assertThat(accountPerformance.getFinalAccumulatedPercentage(), closeTo(ttwror, 0.0001));

        // if the tax_refund is for a security, it must not be included in the
        // performance of the account
        AccountTransaction tax_refund = account.getTransactions().get(2);
        tax_refund.setSecurity(new Security());

        accountPerformance = PerformanceIndex.forAccount(client, account, period, warnings);
        assertThat(warnings, empty());
        assertThat(accountPerformance.getFinalAccumulatedPercentage(), lessThan(ttwror));
    }
}
