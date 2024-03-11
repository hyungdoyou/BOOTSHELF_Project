package com.example.bootshelf.user.repository.querydsl;


import com.example.bootshelf.user.model.entity.QUser;
import com.example.bootshelf.user.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserRepositoryCustomImpl extends QuerydslRepositorySupport implements UserRepositoryCustom {
    public UserRepositoryCustomImpl() {
        super(User.class);
    }

    @Override
    public Page<User> findUserList(Pageable pageable) {
        QUser user = new QUser("user");


        List<User> result = from(user)
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch().stream().distinct().collect(Collectors.toList());

        return new PageImpl<>(result, pageable, result.size());
    }

    @Override
    public Optional<User> findUser(String email) {
        QUser user = new QUser("user");


        Optional<User> result = Optional.ofNullable(from(user)
                .where(user.email.eq(email))
                .fetchOne()
        );

        return result;
    }
}