package com.codeit_team01.sb07_hrbank_team01.department.repository;

import com.codeit_team01.sb07_hrbank_team01.department.entity.Department;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import static com.codeit_team01.sb07_hrbank_team01.department.entity.QDepartment.department;


public class DepartmentRepositoryImpl implements DepartmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public DepartmentRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    //여기서 department는 QDepartment이다  위에 스태틱 임포트로 받았다
    //department.description 이러면 department테이블의 description 컬럼이라 생각하자

    @Override
    public Page<Department> search(String kw,
                                   String sortField,
                                   String sortDirection,
                                   String cursor,
                                   Long idAfter,
                                   Pageable pageable) {

        // null이면 전체 ,기본값name, asc true기본값
        String keyword = (kw == null || kw.isBlank()) ? null : kw.trim();
        String sf = (sortField == null || sortField.isBlank()) ? "name" : sortField.trim();
        boolean asc = !"desc".equalsIgnoreCase(sortDirection);

        //  where BooleanBuilder 조건이다
        //키워드가 있으면 이름 또는 부서설명 동시에 검사
        //containsIgnoreCase 대소문자 무시하고 %keyword% 하겠다
        BooleanBuilder where = new BooleanBuilder();
        if (keyword != null) {
            where.and(
                    department.name.containsIgnoreCase(keyword)
                            .or(department.description.containsIgnoreCase(keyword))
            );
        }

        //  buildCursorPredicate로 커서가 있으면 분기4개 where 조건 추가
        // 없으면 cursorPred = null 이라  조건추가 x
        BooleanBuilder cursorPred = buildCursorPredicate(sf, asc, cursor, idAfter);
        if (cursorPred != null) where.and(cursorPred);

         /*항상 정렬 컬럼(예: name 또는 establishedDate) 뒤에 id를 두 번째 정렬 키로
          오름차: ORDER BY name ASC id ASC
          내림차: ORDER BY name DESC id DESC*/
        //정렬 기준을 담은 배열
        OrderSpecifier<?>[] orders = buildOrders(sf, asc);

        // 조회 필요한 쿼리 때려박기
        var content = queryFactory
                .selectFrom(department)
                .where(where)
                .orderBy(orders)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Long total = queryFactory
                .select(department.count())
                .from(department)
                .where(keyword == null ? null :
                        department.name.containsIgnoreCase(keyword)
                                .or(department.description.containsIgnoreCase(keyword))
                )
                .fetchOne();
        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    //커서가없으면 아무것도 안한다
    private BooleanBuilder buildCursorPredicate(String sortField, boolean asc, String cursor, Long idAfter) {
        if (cursor == null || cursor.isBlank()) return null;

        BooleanBuilder bb = new BooleanBuilder();
        boolean hasIdAfter = (idAfter != null);

        //여기서 dsl은 타입을 지명해준다
        switch (sortField) {
            case "establishedDate": {
                LocalDate date;
                try { date = LocalDate.parse(cursor.trim());}
                catch (Exception e) { return null; } // 파싱 날짜 형식으로 기입실패하면 조건무시
                //트루는 오름차기본

                if (asc) {
                    if (hasIdAfter) {
                        //where 설정 추가 gt( 커서(설립일)  초과 ) 조건
                        //같다면(eq) id가 커서 id보다 큰(gt) 것만 가져온다
                        bb.and(
                                department.establishedDate.gt(date)
                                        .or(department.establishedDate.eq(date).and(department.id.gt(idAfter)))
                        );
                    } else {
                        // idAfter 없으면 날짜같을때 는 고려 하지않는다
                        bb.and(department.establishedDate.gt(date));
                    }
                } else {
                    //이건 반대로 내림차순  lt(미만)
                    if (hasIdAfter) {
                        bb.and(
                                department.establishedDate.lt(date)
                                        .or(department.establishedDate.eq(date).and(department.id.lt(idAfter)))
                        );
                    } else {
                        bb.and(department.establishedDate.lt(date));
                    }
                }
                break;
            }
            case "name":
            default: {
                String name = cursor.trim();
                if (asc) {
                    if (hasIdAfter) {
                        bb.and(
                                department.name.gt(name)
                                        .or(department.name.eq(name).and(department.id.gt(idAfter)))
                        );
                    } else {
                        bb.and(department.name.gt(name));
                    }
                } else {
                    if (hasIdAfter) {
                        bb.and(
                                department.name.lt(name)
                                        .or(department.name.eq(name).and(department.id.lt(idAfter)))
                        );
                    } else {
                        bb.and(department.name.lt(name));
                    }
                }
                break;
            }
        }
        return bb;
    }

    //기준이 설립일 ,name 둘중 하나니
    private OrderSpecifier<?>[] buildOrders(String sortField, boolean asc) {
        if ("establishedDate".equals(sortField)) {
            return asc ?
                    new OrderSpecifier<?>[]{department.establishedDate.asc(), department.id.asc()} :
                    new OrderSpecifier<?>[]{department.establishedDate.desc(), department.id.desc()};
        } else {
            return asc ?
                    new OrderSpecifier<?>[]{department.name.asc(), department.id.asc()} :
                    new OrderSpecifier<?>[]{department.name.desc(), department.id.desc()};
        }
    }
}
