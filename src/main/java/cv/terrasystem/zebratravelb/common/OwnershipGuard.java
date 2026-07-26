package cv.terrasystem.zebratravelb.common;

import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.user.User;
import org.springframework.security.access.AccessDeniedException;

/**
 * ADMIN can manage anything; an AGENTE can only touch resources they created themselves.
 */
public final class OwnershipGuard {

    private OwnershipGuard() {
    }

    public static void requireOwnerOrAdmin(UserPrincipal principal, User createdBy) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return;
        }
        if (createdBy == null || !createdBy.getId().equals(principal.getId())) {
            throw new AccessDeniedException("Só pode gerir o que criou.");
        }
    }
}
