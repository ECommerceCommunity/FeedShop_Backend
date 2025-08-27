package com.cMall.feedShop.ai.domain.template;

import com.cMall.feedShop.common.ai.PromptTemplates;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class ProductRecommendationTemplate {
    public String buildPrompt(User user, String promptInput, int limit) {
        String profileInfo = buildProfileInfo(user);
        return String.format(PromptTemplates.PRODUCT_RECOMMENDATION_TEMPLATE, limit, promptInput, profileInfo);
    }

    private String buildProfileInfo(User user) {
        StringBuilder prompt = new StringBuilder();
        UserProfile profile = user != null ? user.getUserProfile() : null;
        if (profile != null) {
            prompt.append("\n=== 사용자 정보 ===\n");

            if (profile.getFootSize() != null) {
                prompt.append(String.format("- 발 크기: %dmm\n", profile.getFootSize()));
            }

            if (profile.getFootWidth() != null) {
                prompt.append(String.format("- 발 너비: %s\n", profile.getFootWidth()));
            }

            if (profile.getFootArchType() != null) {
                prompt.append(String.format("- 발등 높이: %s\n", profile.getFootArchType()));
            }

            if (profile.getGender() != null) {
                prompt.append(String.format("- 성별: %s\n", profile.getGender()));
            }

            if (profile.getHeight() != null) {
                prompt.append(String.format("- 키: %dcm\n", profile.getHeight()));
            }

            if (profile.getWeight() != null) {
                prompt.append(String.format("- 체중: %dkg\n", profile.getWeight()));
            }
        }

        return prompt.toString();
    }
}
