package com.luckvicky.blur.domain.leagueboard.service;

import com.luckvicky.blur.domain.board.model.entity.Board;
import com.luckvicky.blur.domain.leagueboard.model.entity.LeagueBoard;
import com.luckvicky.blur.domain.leagueboard.repository.LeagueBoardRepository;
import com.luckvicky.blur.domain.leaguemember.exception.NotAllocatedLeagueException;
import com.luckvicky.blur.domain.leaguemember.repository.LeagueMemberRepository;
import com.luckvicky.blur.domain.like.exception.FailToDeleteLikeException;
import com.luckvicky.blur.domain.like.exception.NotExistLikeException;
import com.luckvicky.blur.domain.like.model.dto.response.LikeCreateResponse;
import com.luckvicky.blur.domain.like.model.dto.response.LikeDeleteResponse;
import com.luckvicky.blur.domain.like.model.dto.response.LikeStatusResponse;
import com.luckvicky.blur.domain.like.model.entity.Like;
import com.luckvicky.blur.domain.like.repository.LikeRepository;
import com.luckvicky.blur.domain.member.model.entity.Member;
import com.luckvicky.blur.domain.member.repository.MemberRepository;
import com.luckvicky.blur.infra.redisson.DistributedLock;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class LeagueLikeServiceImpl implements LeagueLikeService {

    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;
    private final LeagueMemberRepository leagueMemberRepository;
    private final LeagueBoardRepository leagueBoardRepository;

    @Override
    public LikeCreateResponse createLike(UUID memberId, UUID boardId) {

        Member member = memberRepository.getOrThrow(memberId);
        LeagueBoard board = leagueBoardRepository.getOrThrow(boardId);
        isAllocatedLeague(board, member);

        likeRepository.save(
                Like.builder()
                        .member(member)
                        .board(board)
                        .build()
        );

        Boolean isLike = isLike(member, board);
        if (!isLike) {
            throw new FailToDeleteLikeException();
        }

        increaseLikeCount(board);
        return LikeCreateResponse.of(board.getLikeCount(), isLike(member, board));

    }

    @Override
    public LikeStatusResponse getLikeStatusByBoard(UUID memberId, UUID boardId) {

        Member member = memberRepository.getOrThrow(memberId);
        LeagueBoard board = leagueBoardRepository.getOrThrow(boardId);
        isAllocatedLeague(board, member);

        return LikeStatusResponse.of(board.getLikeCount(), isLike(member, board));

    }

    @Override
    public LikeDeleteResponse deleteLike(UUID memberId, UUID boardId) {

        Member member = memberRepository.getOrThrow(memberId);
        LeagueBoard board = leagueBoardRepository.getOrThrow(boardId);
        isAllocatedLeague(board, member);

        Like findLike = likeRepository.findByMemberAndBoard(member, board)
                .orElseThrow(NotExistLikeException::new);

        likeRepository.deleteById(findLike.getId());

        Boolean isLike = isLike(member, board);
        if (isLike) {
            throw new FailToDeleteLikeException();
        }

        decreaseLikeCount(board);
        return LikeDeleteResponse.of(board.getLikeCount(), isLike);

    }

    @DistributedLock(value = "#boardId")
    private static void increaseLikeCount(Board board) {
        board.increaseLikeCount();
    }

    @DistributedLock(value = "#boardId")
    private static void decreaseLikeCount(Board board) {
        board.decreaseLikeCount();
    }

    private void isAllocatedLeague(LeagueBoard board, Member member) {
        if (!leagueMemberRepository.existsByLeagueAndMember(board.getLeague(), member)) {
            throw new NotAllocatedLeagueException();
        }
    }

    private Boolean isLike(Member member, Board board) {
        return likeRepository.existsByMemberAndBoard(member, board);
    }

}
