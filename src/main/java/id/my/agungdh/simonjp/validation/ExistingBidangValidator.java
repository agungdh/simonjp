package id.my.agungdh.simonjp.validation;

import id.my.agungdh.simonjp.repository.BidangRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExistingBidangValidator implements ConstraintValidator<ExistingBidang, UUID> {

    @Autowired
    private BidangRepository repository;

    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return repository.findByUuid(value).isPresent();
    }
}