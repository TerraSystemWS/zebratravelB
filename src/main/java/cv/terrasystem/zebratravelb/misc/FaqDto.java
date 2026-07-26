package cv.terrasystem.zebratravelb.misc;

public record FaqDto(Integer id, String tab, String question, String answer) {
    public static FaqDto from(Faq faq) {
        return new FaqDto(faq.getId(), faq.getTab().getLabel(), faq.getQuestion(), faq.getAnswer());
    }
}
