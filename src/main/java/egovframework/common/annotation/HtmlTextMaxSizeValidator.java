package egovframework.common.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import egovframework.common.util.HtmlUtil;

public class HtmlTextMaxSizeValidator implements ConstraintValidator<HtmlTextMaxSize, String> {

    private int max;
    private int maxHtml;

    @Override
    public void initialize(HtmlTextMaxSize constraintAnnotation) {
        this.max = constraintAnnotation.max();
        this.maxHtml = constraintAnnotation.maxHtml();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        if (maxHtml > 0 && value.length() > maxHtml) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "내용이 너무 깁니다. (HTML 최대 " + maxHtml + "자)")
                .addConstraintViolation();
            return false;
        }
        String plainText = HtmlUtil.stripHtml(value);
        return plainText.length() <= max;
    }
}
