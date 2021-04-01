package warehouse.statistics;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UnauthorizedErrorService {

   private Set<UnauthorizedError> unauthorizedErrorList;

    public UnauthorizedErrorService() {
        this.unauthorizedErrorList = new HashSet<>();
    }

    public Set<UnauthorizedError> getUnauthorizedErrorList() {
        return unauthorizedErrorList;
    }

    public UnauthorizedErrorService setUnauthorizedErrorList(Set<UnauthorizedError> unauthorizedErrorList) {
        this.unauthorizedErrorList = unauthorizedErrorList;
        return this;
    }
}
