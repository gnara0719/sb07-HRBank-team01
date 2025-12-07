package com.codeit_team01.sb07_hrbank_team01.history.repository;

import com.codeit_team01.sb07_hrbank_team01.common.dto.response.PageResponseDto;
import com.codeit_team01.sb07_hrbank_team01.history.dto.requestDto.HistorySearchCondition;
import com.codeit_team01.sb07_hrbank_team01.history.dto.responseDto.HistoryChangeLogDto;
import com.codeit_team01.sb07_hrbank_team01.history.entity.History;
import com.codeit_team01.sb07_hrbank_team01.history.entity.HistoryType;
import com.codeit_team01.sb07_hrbank_team01.history.utils.CursorUtils;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

import static com.codeit_team01.sb07_hrbank_team01.history.entity.QHistory.history;

@RequiredArgsConstructor
public class HistoryRepositoryImpl implements HistoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public PageResponseDto<HistoryChangeLogDto> searchHistoriesWithCursor(HistorySearchCondition condition) {
        int pageSize = condition.getPageSize();
        String sortField = condition.getSortField();
        boolean asc = condition.isAscending();

        //데이터 조회
        List<History> histories = queryFactory
                .selectFrom(history)
                .where(
                        //검색 조건(AND)
                        employeeNumberContains(condition.getEmployeeNo()),
                        memoContains(condition.getMemo()),
                        ipAddressContains(condition.getIpAddress()),
                        createAtBetween(condition.getAtFrom(), condition.getAtTo()),
                        typeEquals(condition.getType()),
                        //커서조건
                        cursorCondition(condition.getCursorId(), asc)
                )
                .orderBy(getOrderSpecifiers(sortField, asc))
                .limit(pageSize + 1)
                .fetch();

        //다음 페이지 존재 여부 판단
        boolean hasNext = histories.size() > pageSize;

        //실제 반환 데이터 (pageSize만큼만 잘라냄)
        List<History> content = hasNext ? histories.subList(0, pageSize) : histories;

        // DTO변환
        List<HistoryChangeLogDto> dtoList = content.stream()
                .map(HistoryChangeLogDto::from)
                .toList();

        //전체 개수 조회
        Long totalElements = queryFactory
                .select(history.count())
                .from(history)
                .where(
                        //커서조건 제외, 검색 조건만 허용
                        employeeNumberContains(condition.getEmployeeNo()),
                        memoContains(condition.getMemo()),
                        ipAddressContains(condition.getIpAddress()),
                        createAtBetween(condition.getAtFrom(), condition.getAtTo()),
                        typeEquals(condition.getType())
                )
        .fetchOne();

        // 다음 커서 계산
        Object nextCursor = null;
        Long nextIdAfter = null;

        if(hasNext && !dtoList.isEmpty()){
            nextIdAfter = dtoList.get(dtoList.size() - 1).id();
            nextCursor = CursorUtils.encodeCursor(nextIdAfter);
        }

        //PageResponseDto
        return new PageResponseDto<>(
                dtoList,
                nextCursor,
                nextIdAfter,
                dtoList.size(),
                totalElements != null ? totalElements : 0L,
                hasNext
        );
    }

    //사번 부분 일치
    private BooleanExpression employeeNumberContains(String employeeNo) {
        return (employeeNo == null || employeeNo.isBlank())
                ? null : history.employeeNo.contains(employeeNo);
    }
    //메모 부분 일치
    private BooleanExpression memoContains(String memo) {
        return (memo == null || memo.isBlank())
                ? null : history.memo.contains(memo);
    }
    //ip주소 부분 일치
    private BooleanExpression ipAddressContains(String ipAddress) {
        return (ipAddress == null || ipAddress.isBlank())
                ? null : history.ipAddress.contains(ipAddress);
    }
    //날짜 범위
    private BooleanExpression createAtBetween(Instant atFrom, Instant atTo) {
        if(atFrom != null && atTo != null){
            return history.createdAt.between(atFrom, atTo);
        }else if(atFrom != null){
            return history.createdAt.goe(atFrom);
        }else if(atTo != null){
            return history.createdAt.loe(atTo);
        }
        return null;
    }
    private BooleanExpression typeEquals(HistoryType type) {
        return type == null ? null : history.type.eq(type);
    }

    //커서 조건
    private BooleanExpression cursorCondition(Long cursorId, boolean asc) {
        return cursorId == null ? null : (asc ? history.id.gt(cursorId) : history.id.lt(cursorId));
    }

    //정렬 조건 생성
    private OrderSpecifier<?>[] getOrderSpecifiers(String sortField, boolean asc) {
        ComparableExpressionBase<?> field = "ipAddress".equals(sortField)
                ? history.ipAddress : history.createdAt;
        return new OrderSpecifier<?>[] {
                asc ? field.asc() : field.desc(),
                asc ? history.id.asc() : history.id.desc()
        };
    }
}
