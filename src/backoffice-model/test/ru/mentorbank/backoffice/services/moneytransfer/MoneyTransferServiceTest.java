package ru.mentorbank.backoffice.services.moneytransfer;

import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;

import ru.mentorbank.backoffice.dao.OperationDao;
import ru.mentorbank.backoffice.dao.exception.OperationDaoException;
import ru.mentorbank.backoffice.model.Operation;
import ru.mentorbank.backoffice.model.stoplist.JuridicalStopListRequest;
import ru.mentorbank.backoffice.model.stoplist.PhysicalStopListRequest;
import ru.mentorbank.backoffice.model.stoplist.StopListStatus;
import ru.mentorbank.backoffice.model.transfer.JuridicalAccountInfo;
import ru.mentorbank.backoffice.model.transfer.PhysicalAccountInfo;
import ru.mentorbank.backoffice.model.transfer.TransferRequest;
import ru.mentorbank.backoffice.services.accounts.AccountServiceBean;
import ru.mentorbank.backoffice.services.moneytransfer.exceptions.TransferException;
import ru.mentorbank.backoffice.services.stoplist.StopListServiceStub;
import ru.mentorbank.backoffice.test.AbstractSpringTest;

public class MoneyTransferServiceTest extends AbstractSpringTest {

	@Autowired
	private MoneyTransferService moneyTransferService;
	private PhysicalAccountInfo srcAccount;
	private JuridicalAccountInfo dstAccount;
	private TransferRequest transferReq;
	private AccountServiceBean mockedAcService;
	private OperationDao mockedOpDao;
	private StopListServiceStub mockedStService;
	private String SA_NUM;
	private String DA_NUM;


	@Before
	public void setUp() {
		srcAccount = new PhysicalAccountInfo();
	   	dstAccount = new JuridicalAccountInfo();
	   	transferReq = new TransferRequest();
	   	SA_NUM = "777";
	   	DA_NUM = "888";
	   	mockedAcService = mock(AccountServiceBean.class);
	   	mockedStService = spy(new StopListServiceStub());
	   	mockedOpDao = mock(OperationDao.class);
 
	   	srcAccount.setAccountNumber(SA_NUM);
	   	srcAccount.setDocumentSeries(StopListServiceStub.DOC_SERIES_FOR_OK_STATUS);
	    
	   	dstAccount.setAccountNumber(DA_NUM);
	   	dstAccount.setInn(StopListServiceStub.INN_FOR_OK_STATUS);
	   	
	   	transferReq.setSrcAccount(srcAccount);
	   	transferReq.setDstAccount(dstAccount);

	   	when(mockedAcService.verifyBalance(srcAccount)).thenReturn(true);
	   	  ((MoneyTransferServiceBean) moneyTransferService).setAccountService(mockedAcService);
	   	  ((MoneyTransferServiceBean) moneyTransferService).setStopListService(mockedStService);
	      ((MoneyTransferServiceBean) moneyTransferService).setOperationDao(mockedOpDao);
	}

	@Test
	public void transfer() throws TransferException, OperationDaoException {
		// TODO: Необходимо протестировать, что для хорошего перевода всё
		// работает и вызываются все необходимые методы сервисов
		// Далее следует закомментированная заготовка
		// StopListService mockedStopListService =
		// mock(StopListServiceStub.class);
		// AccountService mockedAccountService = mock(AccountServiceBean.class);
		//
		// moneyTransferService.transfer(null);
		//
		// verify(mockedStopListService).getJuridicalStopListInfo(null);
		// verify(mockedAccountService).verifyBalance(null);
		moneyTransferService.transfer(transferReq);
		verify(mockedStService).getJuridicalStopListInfo(argThat(new ArgumentMatcher<JuridicalStopListRequest>()
		{
		public boolean matches(Object v) {
			if (v instanceof JuridicalStopListRequest) {
			JuridicalStopListRequest request = (JuridicalStopListRequest) v;
			return true;
			}
		   	return false;
		 }
		 }));
		
		verify(mockedStService).getPhysicalStopListInfo(argThat(new ArgumentMatcher<PhysicalStopListRequest>() {
		@Override
		public boolean matches(Object i) {
			if (i instanceof PhysicalStopListRequest) {
		   	PhysicalStopListRequest request = (PhysicalStopListRequest) i;
		   	if ((request.getDocumentSeries() == StopListServiceStub.DOC_SERIES_FOR_OK_STATUS))
		   	return true;
		   	}
			return false;
		 }
		 }));
		
		verify(mockedAcService).verifyBalance(srcAccount);
		verify(mockedOpDao).saveOperation(argThat(new ArgumentMatcher<Operation>(){

			@Override
			public boolean matches(Object argument){
				if (argument instanceof Operation) {
				Operation operation = (Operation)argument;
				if ((operation.getSrcAccount().getAccountNumber() == SA_NUM) &&
					(operation.getDstAccount().getAccountNumber() == DA_NUM) &&
					(operation.getSrcStoplistInfo().getStatus() == StopListStatus.OK))
				return true;
				}
				
				return false;
			}}));
			}
	}
