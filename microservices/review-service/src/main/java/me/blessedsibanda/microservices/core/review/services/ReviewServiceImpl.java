package me.blessedsibanda.microservices.core.review.services;

import me.blessedsibanda.api.core.review.Review;
import me.blessedsibanda.api.core.review.ReviewService;
import me.blessedsibanda.api.exceptions.InvalidInputException;
import me.blessedsibanda.microservices.core.review.persistence.ReviewEntity;
import me.blessedsibanda.microservices.core.review.persistence.ReviewRepository;
import me.blessedsibanda.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final ReviewRepository repository;
    private final ReviewMapper mapper;

    @Autowired
    public ReviewServiceImpl(ServiceUtil serviceUtil,
                             ReviewRepository repository,
                             ReviewMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getReviews: response size: {}", list.size());
        return list;
    }

    @Override
    public Review createReview(Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            LOG.debug("createReview: created a review entity: {}/{}",
                    body.getProductId(), body.getReviewId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Product Id: "
                    + body.getProductId() + ", Review Id: " + body.getReviewId());
        }
    }

    @Override
    public void deleteReviews(int productId) {
        LOG.debug(
                "deleteReviews: tries to delete reviews for the product with productId: {}",
                productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
