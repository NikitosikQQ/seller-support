package ru.seller_support.assignment.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.domain.PostingInfoModel;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrderFilterService {

    public List<PostingInfoModel> filterOrdersByWrong(List<PostingInfoModel> postings, boolean needWrong, boolean filterWrongBox) {
        Stream<PostingInfoModel> stream = postings.stream().filter(post -> needWrong == post.getProduct().getWrongArticle());
        return filterWrongBox
                ? stream.filter(post -> post.getProduct().getWrongBox().equals(Boolean.FALSE)).toList()
                : stream.toList();
    }
}
