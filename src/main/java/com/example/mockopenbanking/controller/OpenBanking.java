package com.example.mockopenbanking.controller;

import com.example.mockopenbanking.dto.*;
import com.example.mockopenbanking.repositories.ConsentRepository;
import com.example.mockopenbanking.repositories.FinRepository;
import com.example.mockopenbanking.services.RejectionService;
import com.example.mockopenbanking.services.SendOtpService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequestMapping("/open-banking")
@RequiredArgsConstructor
public class OpenBanking {

    /* ── status constants ─────────────────────────────────────────── */
    private static final String STATUS_PENDING            = "PENDING";
    private static final String STATUS_AWAITING_SIGNATURE = "AWAITING_SIGNATURE";
    private static final String VALID  = "VALID";
    private static final String REJECTED = "REJECTED";

    /* ── injected collaborators ───────────────────────────────────── */
    private final RejectionService  rejectionService;
    private final SendOtpService    sendOtpService;
    private final ConsentRepository consentRepository;
    private final FinRepository     finRepository;

    /* ─────────────────────────── 1. FIRST PASS ───────────────────── */
    @PostMapping("/first_pass")
    public ResponseEntity<?> firstPass(@RequestBody FirstRequestCheck req) {

        if (hasNull(req)) {
            return badRequest("One of the required fields is null");
        }
        DocsResponse docsResponse = finRepository.findById(req.getFin()).orElse(null);
        if(docsResponse == null){
            return badRequest("Did not find FIN in DB");
        }
        Consent consent = new Consent();
        consent.setId(req.getConsentId());
        consent.setStatus(STATUS_PENDING);
        consent.setPhone_number(req.getPhone_number());
        consent.setIBAN(req.getIban());
        consentRepository.save(consent);

        return ok("Request approved");
    }

    /* ────────────────────────── 2. SECOND PASS ───────────────────── */
    @PostMapping("/second_pass")
    public ResponseEntity<?> secondPass(@RequestHeader String consentId,
                                        @RequestBody LoginRequest req) {
            Consent consent = consentRepository.findById(consentId)
                .orElse(null);

        if (consent == null) {
            return unauthorized("Session id not found");
        }
        if (!STATUS_PENDING.equals(consent.getStatus())) {
            return unauthorized("Illegal move");
        }
        if (req.getFin() == null || req.getPhone_number() == null) {
            return rejectAndBad(consentId, "Some or all required parameters are null",consent);
        }
        if (!sendOtpService.send_message(req.getPhone_number())) {
            return rejectAndUnauthorized(consentId, "Did not pass OTP check",consent);
        }

        DocsResponse docs = finRepository.findById(req.getFin())
                .orElseThrow(() -> new IllegalArgumentException("FIN record not found"));
        docs.setTerms_and_conditions("Some doc with term and conditions goes here.");
        docs.setPeriod(Duration.ofDays(180));

        consent.setStatus(STATUS_AWAITING_SIGNATURE);
        consentRepository.save(consent);
        rejectionService.startSignatureWindow(consentId);

        return ResponseEntity.ok(docs);
    }

    /* ──────────────────────── 3. SIGNATURE RESULT ───────────────── */
    @PostMapping("/signature/result")
    public ResponseEntity<?> signatureResult(@RequestHeader String consentId,
                                             @RequestBody SignatureResponse sig) {

        Consent consent = consentRepository.findById(consentId)
                .orElse(null);

        if (consent == null) {
            return unauthorized("Consent id is not valid");
        }
        if (!STATUS_AWAITING_SIGNATURE.equals(consent.getStatus())) {
            return unauthorized("Illegal move");
        }
        if (!sig.result) {
            return rejectAndNotAcceptable(consentId, "Your session is deleted",consent);
        }
        if (!sendOtpService.send_message(consent.getPhone_number())) {
            return rejectAndUnauthorized(consentId, "Did not pass OTP check",consent);
        }

        rejectionService.acceptSignature(consentId);
        consent.setStatus(VALID);

        consentRepository.save(consent);
        FinalApprovalResponse resp = new FinalApprovalResponse();
        resp.setIBAN(consent.getIBAN());
        resp.setPeriod(Duration.ofDays(180));
        resp.setConsentId(consentId);

        return ResponseEntity.ok(resp);
    }

    /* ───────────────────────── helper utilities ──────────────────── */

    private static boolean hasNull(FirstRequestCheck r) {
        return Stream.of(r.getFin(), r.getPhone_number(), r.getCurrency(),
                        r.getIban(), r.getConsentId(), r.getBalance())
                .anyMatch(Objects::isNull);
    }

    private ResponseEntity<BankResponse> ok(String msg) {
        return ResponseEntity.ok(new BankResponse(true, msg));
    }

    private ResponseEntity<BankResponse> badRequest(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BankResponse(false, msg));
    }

    private ResponseEntity<BankResponse> unauthorized(String msg) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new BankResponse(false, msg));
    }

    private ResponseEntity<BankResponse> notAcceptable(String msg) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(new BankResponse(false, msg));
    }

    private ResponseEntity<BankResponse> rejectAndBad(String id, String msg,Consent consent) {
        consent.setStatus(REJECTED);
        consentRepository.save(consent);
        rejectionService.reject(id);
        return badRequest(msg);
    }

    private ResponseEntity<BankResponse> rejectAndUnauthorized(String id, String msg,Consent consent) {
        consent.setStatus(REJECTED);
        consentRepository.save(consent);
        rejectionService.reject(id);
        return unauthorized(msg);
    }

    private ResponseEntity<BankResponse> rejectAndNotAcceptable(String id, String msg,Consent consent) {
        consent.setStatus(REJECTED);
        consentRepository.save(consent);
        rejectionService.reject(id);
        return notAcceptable(msg);
    }
}
