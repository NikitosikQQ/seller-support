package ru.seller_support.assignment.adapter.marketplace.ozon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.inner.Posting;
import ru.seller_support.assignment.adapter.marketplace.ozon.dto.inner.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class OzonPostingSplitter {

    public List<Posting> splitOrders(List<Posting> postings) {
        List<Posting> result = new ArrayList<>();

        for (Posting posting : postings) {
            if (posting.getProducts().size() <= 1) {
                result.add(posting);
            } else {
                for (Product product : posting.getProducts()) {
                    Posting newPosting = posting.toBuilder()
                            .products(Collections.singletonList(product))
                            .build();
                    result.add(newPosting);
                }
            }
        }

        return result;
    }
}
