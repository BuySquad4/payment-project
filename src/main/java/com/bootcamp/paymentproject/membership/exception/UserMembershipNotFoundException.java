package com.bootcamp.paymentproject.membership.exception;

import com.bootcamp.paymentproject.common.exception.ErrorCode;

public class UserMembershipNotFoundException extends MembershipException {
    public UserMembershipNotFoundException() {
        super(ErrorCode.USER_MEMBERSHIP_NOT_FOUND);
    }
}
