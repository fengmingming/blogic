package blogic.productline.iteration.service;

import blogic.productline.iteration.domain.repository.IterationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IterationService {

    @Autowired
    private IterationRepository iterationRepository;



}
