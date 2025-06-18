package com.example.mockopenbanking;
import com.example.mockopenbanking.dto.*;

import com.example.mockopenbanking.controller.OpenBanking;
import com.example.mockopenbanking.dto.BankResponse;
import com.example.mockopenbanking.dto.FirstRequestCheck;
import com.example.mockopenbanking.dto.LoginRequest;
import com.example.mockopenbanking.repositories.ConsentRepository;
import com.example.mockopenbanking.repositories.FinRepository;
import com.example.mockopenbanking.services.RejectionService;
import com.example.mockopenbanking.services.SendOtpService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class MockOpenBankingApplicationTests {
	@Mock
	SignatureResponse signatureResponse;
	@Mock
	RejectionService rejectionService;
	@Mock Consent consent;
	@Mock
	SendOtpService sendOtpService;
	@Mock
	ConsentRepository consentRepository;
	@Mock
	DocsResponse docsResponse;
	@Mock
	FinRepository finRepository;
	@InjectMocks
	private OpenBanking openBankingcontroller;
	@Test
	void test_FirstPassHappyEnding() {
		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here

		FirstRequestCheck baseRequest = new FirstRequestCheck();
		baseRequest.setBalance("100.00");
		baseRequest.setCurrency("AZN");
		baseRequest.setIban("ADR2332");
		baseRequest.setFin("FIN12344");
		baseRequest.setPhone_number("050-321-21-21");
		baseRequest.setConsentId("c-001");
		docsResponse.setIBAN("frdewsq");
		docsResponse.setFin("FIN12334");
		docsResponse.setBalance("123.00");
		when(finRepository.findById(any(String.class))).thenReturn(Optional.ofNullable(docsResponse));
		ResponseEntity<?> responseEntity = openBankingcontroller.firstPass(baseRequest);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
		BankResponse bankResponse = (BankResponse) responseEntity.getBody();
		assertThat(bankResponse.getMessage())
				.isEqualTo("Request approved");

	}
	@Test
	void test_DocsResponseError(){
		FirstRequestCheck baseRequest = new FirstRequestCheck();
		baseRequest.setBalance("100.00");
		baseRequest.setCurrency("AZN");
		baseRequest.setIban("ADR2332");
		baseRequest.setFin("FIN12344");
		baseRequest.setPhone_number("050-321-21-21");
		baseRequest.setConsentId("c-001");
		ResponseEntity<?> responseEntity = openBankingcontroller.firstPass(baseRequest);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		BankResponse bankResponse = (BankResponse) responseEntity.getBody();
		assertThat(bankResponse.getMessage())
				.isEqualTo("Did not find FIN in DB");

	}
	@Test
	void test_FirstPassReqisNull() {
		FirstRequestCheck baseRequest = new FirstRequestCheck();

		ResponseEntity<?> responseEntity = openBankingcontroller.firstPass(baseRequest);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		BankResponse bankResponse = (BankResponse) responseEntity.getBody();
		assertThat(bankResponse.getMessage())
				.isEqualTo("One of the required fields is null");

	}

	@Test
	void test_SecondPassConsentisNull(){
		String consentID = "123";
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setFin("FIN123");
		loginRequest.setPhone_number("055-231-23-21");
		ResponseEntity<?> responseEntity = openBankingcontroller.secondPass(consentID,loginRequest);
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		BankResponse bankResponse = (BankResponse) responseEntity.getBody();
		assertThat(bankResponse.getMessage())
				.isEqualTo("Session id not found");


	}
	@Test
	void test_SecondPass_InvalidStatus() {

		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here

		// Arrange
		String consentId = "c-001";
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setFin("FIN123456");
		loginRequest.setPhone_number("055-322-12-21");
		Consent consentMock = mock(Consent.class);
		consentMock.setStatus("notPENDIN");
		consentMock.setId("c-001");
		when(consentMock.getStatus()).thenReturn("INVALID_STATUS");
		when(consentRepository.findById(any())).thenReturn(Optional.of(consentMock));

		// Act
		ResponseEntity<?> response = openBankingcontroller.secondPass(consentId, loginRequest);


		// Assert
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		BankResponse body = (BankResponse) response.getBody();
		assertThat(body.getMessage()).isEqualTo("Illegal move");
	}
	@Test
	void test_SecondPass_Phone_Number_Not_Found(){
		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here
		String consentId = "c-001";
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setFin("FIN123456");
		Consent consentMock = mock(Consent.class);
		consentMock.setStatus("PENDING");
		consentMock.setId("c-001");
		when(consentMock.getStatus()).thenReturn("PENDING");
		when(consentRepository.findById(any())).thenReturn(Optional.of(consentMock));
		ResponseEntity<?> response = openBankingcontroller.secondPass(consentId, loginRequest);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		BankResponse body = (BankResponse) response.getBody();
		assertThat(body.getMessage()).isEqualTo("Some or all required parameters are null");

	}
	@Test
	void test_SecondPass_FIN_Not_Found(){
		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here
		String consentId = "c-001";
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setPhone_number("050-323-11-22");
		Consent consentMock = mock(Consent.class);
		consentMock.setStatus("PENDING");
		consentMock.setId("c-001");
		when(consentMock.getStatus()).thenReturn("PENDING");
		when(consentRepository.findById(any())).thenReturn(Optional.of(consentMock));
		ResponseEntity<?> response = openBankingcontroller.secondPass(consentId, loginRequest);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		BankResponse body = (BankResponse) response.getBody();
		assertThat(body.getMessage()).isEqualTo("Some or all required parameters are null");

	}
	@Test
	void test_SecondPass_OTPReject(){
		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here
		String consentId = "c-001";
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setFin("FIN123456");
		loginRequest.setPhone_number("051-234-21-23");
		Consent consentMock = mock(Consent.class);
		consentMock.setStatus("PENDING");
		consentMock.setId("c-001");
		when(consentMock.getStatus()).thenReturn("PENDING");
		when(consentRepository.findById(any())).thenReturn(Optional.of(consentMock));
		when(sendOtpService.send_message(any(String.class))).thenReturn(false);
		ResponseEntity<?> response = openBankingcontroller.secondPass(consentId, loginRequest);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		BankResponse body = (BankResponse) response.getBody();
		assertThat(body.getMessage()).isEqualTo("Did not pass OTP check");

	}
	@Test
	void test_SecondPass_HappyEnding(){
		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here
		String consentId = "c-001";
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setFin("FIN123456");
		loginRequest.setPhone_number("051-234-21-23");
		Consent consentMock = mock(Consent.class);
		consentMock.setStatus("PENDING");
		consentMock.setId("c-001");
		when(consentMock.getStatus()).thenReturn("PENDING");
		when(consentRepository.findById(any())).thenReturn(Optional.of(consentMock));
		when(sendOtpService.send_message(any(String.class))).thenReturn(true);
		docsResponse.setIBAN("frdewsq");
		docsResponse.setFin("FIN12334");
		docsResponse.setBalance("123.00");
		when(finRepository.findById(any(String.class))).thenReturn(Optional.ofNullable(docsResponse));
		ResponseEntity<?> response = openBankingcontroller.secondPass(consentId, loginRequest);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


	}
	@Test
	void test_signatureResultConsentNull(){
		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here
		when(consentRepository.findById(any())).thenReturn(Optional.ofNullable(null));
		String consentId = "c-001";
		signatureResponse.result = true;
		ResponseEntity<?> response = openBankingcontroller.signatureResult(consentId, signatureResponse);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		BankResponse body = (BankResponse) response.getBody();
		assertThat(body.getMessage()).isEqualTo("Consent id is not valid");


	}
	@Test
    void test_signatureResultStatusAwaitingSignature(){
		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here
		String consentId = "c-001";
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setFin("FIN123456");
		loginRequest.setPhone_number("051-234-21-23");
		Consent consentMock = mock(Consent.class);
		consentMock.setStatus("PENDING");
		consentMock.setId("c-001");
		signatureResponse.result = true;
		when(consentMock.getStatus()).thenReturn("PENDING");
		when(consentRepository.findById(any())).thenReturn(Optional.of(consentMock));
		ResponseEntity<?> response = openBankingcontroller.signatureResult(consentId, signatureResponse);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		BankResponse body = (BankResponse) response.getBody();
		assertThat(body.getMessage()).isEqualTo("Illegal move");
	}
	@Test
	void test_signatureResultSigisFalse(){
		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here
		String consentId = "c-001";
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setFin("FIN123456");
		loginRequest.setPhone_number("051-234-21-23");
		Consent consentMock = mock(Consent.class);
		consentMock.setStatus("PENDING");
		consentMock.setId("c-001");
		signatureResponse.result = false;
		when(consentMock.getStatus()).thenReturn("AWAITING_SIGNATURE");
		when(consentRepository.findById(any())).thenReturn(Optional.of(consentMock));
		ResponseEntity<?> response = openBankingcontroller.signatureResult(consentId, signatureResponse);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_ACCEPTABLE);
		BankResponse body = (BankResponse) response.getBody();
		assertThat(body.getMessage()).isEqualTo("Your session is deleted");
	}
	@Test
	void test_signatureResult_OTPReject(){
		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here
		String consentId = "c-001";
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setFin("FIN123456");
		loginRequest.setPhone_number("051-234-21-23");
		Consent consentMock = mock(Consent.class);
		consentMock.setStatus("PENDING");
		consentMock.setId("c-001");
		consentMock.setPhone_number("070-312-21-22");
		signatureResponse.result = true;
		when(consentMock.getStatus()).thenReturn("AWAITING_SIGNATURE");
		when(consentRepository.findById(any())).thenReturn(Optional.of(consentMock));
		when(sendOtpService.send_message(any(String.class))).thenReturn(false);
		when(consentMock.getPhone_number()).thenReturn("050-123-45-67");

		ResponseEntity<?> response = openBankingcontroller.signatureResult(consentId, signatureResponse);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		BankResponse body = (BankResponse) response.getBody();
		assertThat(body.getMessage()).isEqualTo("Did not pass OTP check");
	}
	@Test
	void test_signatureResult_HappyEnding(){
		openBankingcontroller = new OpenBanking(rejectionService,sendOtpService,consentRepository,finRepository); // ✅ inject mock here
		String consentId = "c-001";
		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setFin("FIN123456");
		loginRequest.setPhone_number("051-234-21-23");
		Consent consentMock = mock(Consent.class);
		consentMock.setStatus("PENDING");
		consentMock.setId("c-001");
		consentMock.setPhone_number("070-312-21-22");
		signatureResponse.result = true;
		when(consentMock.getStatus()).thenReturn("AWAITING_SIGNATURE");
		when(consentRepository.findById(any())).thenReturn(Optional.of(consentMock));
		when(sendOtpService.send_message(any(String.class))).thenReturn(true);
		when(consentMock.getPhone_number()).thenReturn("050-123-45-67");
		ResponseEntity<?> response = openBankingcontroller.signatureResult(consentId, signatureResponse);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


	}

}
