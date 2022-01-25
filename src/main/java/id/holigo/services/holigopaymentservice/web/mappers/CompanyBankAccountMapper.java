package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.CompanyBankAccount;
import id.holigo.services.holigopaymentservice.web.model.CompanyBankAccountDto;

@Mapper
public interface CompanyBankAccountMapper {
    CompanyBankAccountDto companyBankAccountToCompanyBankAccountDto(CompanyBankAccount companyBankAccount);
}
