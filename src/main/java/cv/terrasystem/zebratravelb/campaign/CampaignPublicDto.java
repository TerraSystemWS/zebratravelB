package cv.terrasystem.zebratravelb.campaign;

public record CampaignPublicDto(
        Integer id,
        String imageUrl,
        String altText,
        String title,
        String subtitle,
        String linkUrl,
        String ribbon
) {
    public static CampaignPublicDto from(Campaign c, CampaignService.CampaignDisplay display) {
        return new CampaignPublicDto(
                c.getId(), c.getImageUrl(), c.getAltText(),
                display.title(), display.subtitle(), display.linkUrl(), display.ribbon()
        );
    }
}
