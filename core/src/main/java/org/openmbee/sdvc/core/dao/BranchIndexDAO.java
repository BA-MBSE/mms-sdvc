package org.openmbee.sdvc.core.dao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.openmbee.sdvc.json.BaseJson;
import org.openmbee.sdvc.json.RefJson;

public interface BranchIndexDAO {

    void indexAll(Collection<? extends BaseJson> jsons);

    void index(BaseJson json);

    Optional<RefJson> findById(String docId);

    List<RefJson> findAllById(Set<String> docIds);

    void deleteById(String docId);

    void deleteAll(Collection<? extends BaseJson> jsons);

    boolean existsById(String docId);

    RefJson update(RefJson refJson);
}
