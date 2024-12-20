package com.shallwecode.backend.problem.domain.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shallwecode.backend.problem.application.dto.problem.*;
import com.shallwecode.backend.problem.domain.aggregate.*;
import com.shallwecode.backend.problem.domain.repository.ProblemRepository;
import com.shallwecode.backend.problem.domain.repository.TestcaseRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemDomainService {

    private final ProblemRepository repository;
    private final TestcaseRepository testCaseRepository;
    private final ModelMapper modelMapper;
    private final JPAQueryFactory queryFactory;

    @Transactional
    public void saveProblem(ProblemReqDTO newProblem) {

        Problem problem = modelMapper.map(newProblem, Problem.class);
        repository.save(problem);

        // 연관된 테스트 케이스 저장
        newProblem.getTestcases().forEach(testcaseReqDTO -> {
            Testcase testcase = modelMapper.map(testcaseReqDTO, Testcase.class);
            testcase.updateProblemId(problem.getProblemId()); // 문제 ID 설정
            testCaseRepository.save(testcase);
        });
    }

    @Transactional
    public void updateProblem(Long id, ProblemReqDTO updateProblem) {

        // 문제를 조회하고 제목, 내용, 난이도 업데이트
        Problem foundProblem = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 id에 맞는 문제가 존재하지 않습니다."));
        foundProblem.updateProblemTitle(updateProblem.getTitle());
        foundProblem.updateProblemContent(updateProblem.getContent());
        foundProblem.updateProblemProblemLevel(updateProblem.getProblemLevel());

        // 기존 테스트 케이스 삭제 후 새로운 테스트 케이스 추가
        testCaseRepository.deleteByProblemId(id);
        updateProblem.getTestcases().forEach(testcaseReqDTO -> {
            Testcase testcase = modelMapper.map(testcaseReqDTO, Testcase.class);
            testcase.updateProblemId(foundProblem.getProblemId()); // 문제 ID 설정
            testCaseRepository.save(testcase);
        });
    }

    @Transactional
    public void deleteProblem(Long problemId) {

        // 문제와 연관된 테스트 케이스 모두 삭제 후 문제 삭제
        testCaseRepository.deleteByProblemId(problemId);
        repository.deleteById(problemId);
    }

    // 시도하지 않은 문제 개수 조회
    public Long findAllNoTryProblemCount(Long userId) {
        QProblem qProblem = QProblem.problem;
        QTry qTry = QTry.try$;

        return queryFactory.select(qProblem.count())
                .from(qProblem)
                .leftJoin(qTry).on(qTry.problemId.eq(qProblem.problemId)
                        .and(qTry.userId.eq(userId)
                                .or(qTry.coopList.contains("," + userId + ",")
                                        .or(qTry.coopList.startsWith(userId + ",")
                                                .or(qTry.coopList.endsWith("," + userId)
                                                        .or(qTry.coopList.eq(String.valueOf(userId))))))))
                .where(qTry.problemId.isNull()) // 유저와 협업 친구가 시도하지 않은 문제만 선택
                .fetchOne();
    }

    // 시도했지만, 해결하지 못한 문제 개수 조회
    public Long findAllUnSolvedProblemCount(Long userId) {
        QProblem qProblem = QProblem.problem;
        QTry qTry = QTry.try$;

        return queryFactory.select(qProblem.problemId.countDistinct())
                .from(qProblem)
                .innerJoin(qTry).on(qTry.problemId.eq(qProblem.problemId)
                        .and(qTry.isSolved.eq(false))
                        .and(qTry.userId.eq(userId)
                                .or(qTry.coopList.contains("," + userId + ",")
                                        .or(qTry.coopList.startsWith(userId + ",")
                                                .or(qTry.coopList.endsWith("," + userId)
                                                        .or(qTry.coopList.eq(String.valueOf(userId))))))))
                .fetchOne();
    }

    // 해결한 문제 개수 조회
    public Long findAllSolvedProblemCount(long userId) {
        QProblem qProblem = QProblem.problem;
        QTry qTry = QTry.try$;

        return queryFactory.select(qProblem.problemId.countDistinct())
                .from(qProblem)
                .innerJoin(qTry).on(qTry.problemId.eq(qProblem.problemId)
                        .and(qTry.isSolved.eq(true))
                        .and(qTry.userId.eq(userId)
                                .or(qTry.coopList.contains("," + userId + ",")
                                        .or(qTry.coopList.startsWith(userId + ",")
                                                .or(qTry.coopList.endsWith("," + userId)
                                                        .or(qTry.coopList.eq(String.valueOf(userId))))))))
                .fetchOne();
    }

    public ProblemOneResDTO selectOneProblem(Long problemId) {

        QProblem qProblem = QProblem.problem;
        QTestcase qTestcase = QTestcase.testcase;

        // Problem 정보만 조회
        ProblemOneResDTO problemInfo = queryFactory.select(
                        Projections.constructor(ProblemOneResDTO.class,
                                qProblem.problemId,
                                qProblem.title,
                                qProblem.content,
                                qProblem.problemLevel))
                .from(qProblem)
                .where(qProblem.problemId.eq(problemId))
                .fetchOne();

        if (problemInfo == null) {
            return null; // 문제 없음 처리
        }

        // 해당 Problem 에 대한 모든 Testcase 를 조회하여 리스트로 추가
        List<TestcaseDTO> testcases = queryFactory.select(
                        Projections.constructor(TestcaseDTO.class,
                                qTestcase.input,
                                qTestcase.output))
                .from(qTestcase)
                .where(qTestcase.problemId.eq(problemId))
                .fetch();

        problemInfo.setTestcases(testcases);
        return problemInfo;
    }

    @Transactional(readOnly = true)
    public List<FindProblemResDTO> findAllMyProblem(Long userId) {

        QProblem qProblem = QProblem.problem;
        QTry qTry = QTry.try$;

        return queryFactory
                .select(Projections.constructor(FindProblemResDTO.class,
                        qProblem.problemId,
                        qProblem.title,
                        qProblem.problemLevel,
                        QTry.try$.isSolved
                                .when(true).then(1)
                                .otherwise(0)
                                .max()))
                .from(qTry)
                .join(qProblem).on(qTry.problemId.eq(qProblem.problemId))
                .where(qTry.userId.eq(userId))
                .groupBy(qProblem.problemId)
                .orderBy(qTry.createdAt.asc())
                .fetch();
    }

    /* 문제 목록 조회 기능 */
    @Transactional(readOnly=true)
    public List<ProblemDTO> selectProblemList(String keyword, Integer option, Long offset, Long size) {
        QProblem qProblem = QProblem.problem;
        BooleanBuilder whereClause = new BooleanBuilder();

        // 조건 추가 (BooleanBuilder 사용하면 동적으로 조건 추가 가능)
        if (keyword != null) {
            whereClause.and(qProblem.title.like("%" + keyword + "%"));
        }
        if (option != null) {
            whereClause.and(qProblem.problemLevel.eq(option));
        }

        return queryFactory.select(Projections.constructor(ProblemDTO.class,
                        qProblem.problemId,
                        qProblem.title,
                        qProblem.content,
                        qProblem.problemLevel))
                .from(qProblem)
                .where(whereClause)
                .orderBy(qProblem.problemId.asc())
                .limit(size)
                .offset(offset)
                .fetch();
    }

    /* 문제 count 조회 */
    public Long selectProblemCount() {
        QProblem qProblem = QProblem.problem;

        return queryFactory.select(qProblem.problemId.count())
                .from(qProblem)
                .fetchOne();
    }

    @Transactional(readOnly = true)
    public List<FindProblemResDTO> findAllProblem(ProblemSearchFilter filter) {

        QProblem qProblem = QProblem.problem;
        QTry qTry = QTry.try$;

        BooleanBuilder whereConditions = new BooleanBuilder();

        if (filter.getProblemLevel() != null) {
            whereConditions.and(qProblem.problemLevel.eq(filter.getProblemLevel()));
        }

        if (filter.getIsSolved() != null) {
            if (filter.getIsSolved()) {
                whereConditions.and(qTry.isSolved.isTrue());
            } else {
                whereConditions.and(
                        qTry.isSolved.isFalse().or(qTry.isSolved.isNull())
                );
            }
        }

        if (filter.isGuestSearch()) {
            return queryFactory
                    .select(Projections.constructor(FindProblemResDTO.class,
                            qProblem.problemId,
                            qProblem.title,
                            qProblem.problemLevel,
                            Expressions.constant(0)))
                    .from(qProblem)
                    .where(whereConditions)
                    .orderBy(qProblem.problemId.asc())
                    .fetch();
        }
        System.out.println("71434732894782190sdafj");
        return queryFactory
                .select(Projections.constructor(FindProblemResDTO.class,
                        qProblem.problemId,
                        qProblem.title,
                        qProblem.problemLevel,
                        QTry.try$.isSolved
                                .when(true).then(1)
                                .otherwise(0)
                                .max().coalesce(0)))
                .from(qProblem)
                .leftJoin(qTry)
                .on(qTry.problemId.eq(qProblem.problemId)
                        .and(qTry.userId.eq(filter.getUserId())))
                .where(whereConditions)
                .groupBy(qProblem.problemId,
                        qProblem.title,
                        qProblem.problemLevel)
                .orderBy(qProblem.problemId.asc())
                .fetch();
    }
}
