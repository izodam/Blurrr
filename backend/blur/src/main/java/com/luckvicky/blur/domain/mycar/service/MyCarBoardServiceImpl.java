package com.luckvicky.blur.domain.mycar.service;

import com.luckvicky.blur.domain.board.exception.NotExistBoardException;
import com.luckvicky.blur.domain.board.factory.BoardFactory;
import com.luckvicky.blur.domain.board.model.dto.request.BoardCreateRequest;
import com.luckvicky.blur.domain.board.model.entity.Board;
import com.luckvicky.blur.domain.board.model.entity.BoardType;
import com.luckvicky.blur.domain.board.repository.MyCarRepository;
import com.luckvicky.blur.domain.channel.exception.NotExistChannelException;
import com.luckvicky.blur.domain.channel.model.entity.Channel;
import com.luckvicky.blur.domain.channel.repository.ChannelRepository;
import com.luckvicky.blur.domain.channelboard.model.dto.request.MyCarCreateRequest;
import com.luckvicky.blur.domain.channelboard.model.entity.Mention;
import com.luckvicky.blur.domain.channelboard.model.entity.MyCarBoard;
import com.luckvicky.blur.domain.channelboard.repository.MentionRepository;
import com.luckvicky.blur.domain.dashcam.exception.NotFoundDashcamException;
import com.luckvicky.blur.domain.dashcam.model.entity.DashCam;
import com.luckvicky.blur.domain.league.exception.NotExistLeagueException;
import com.luckvicky.blur.domain.league.model.entity.League;
import com.luckvicky.blur.domain.league.repository.LeagueRepository;
import com.luckvicky.blur.domain.like.repository.LikeRepository;
import com.luckvicky.blur.domain.member.model.entity.Member;
import com.luckvicky.blur.domain.member.repository.MemberRepository;
import com.luckvicky.blur.domain.mycar.model.resp.MyCarDetail;
import com.luckvicky.blur.domain.mycar.model.resp.MyCarSimple;
import com.luckvicky.blur.global.enums.status.ActivateStatus;
import com.luckvicky.blur.global.jwt.model.ContextMember;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MyCarBoardServiceImpl implements MyCarBoardService {
    private final MyCarRepository myCarRepository;
    private final Map<BoardType, BoardFactory> factoryMap;
    private final MemberRepository memberRepository;
    private final ChannelRepository channelRepository;
    private final LikeRepository likeRepository;
    private final LeagueRepository leagueRepository;
    private final MentionRepository mentionRepository;


    public MyCarBoardServiceImpl(MyCarRepository myCarRepository, Map<BoardType, BoardFactory> factoryMap,
                                 MemberRepository memberRepository, ChannelRepository channelRepository,
                                 LikeRepository likeRepository, LeagueRepository leagueRepository,
                                 MentionRepository mentionRepository) {
        this.myCarRepository = myCarRepository;
        this.factoryMap = factoryMap;
        this.memberRepository = memberRepository;
        this.channelRepository = channelRepository;
        this.likeRepository = likeRepository;
        this.leagueRepository = leagueRepository;
        this.mentionRepository = mentionRepository;
    }

    @Override
    public Page<MyCarSimple> findMyCars(Pageable page, String keyword) {
        Channel channel = channelRepository
                .findByNameIs(BoardType.MYCAR.getName())
                .orElseThrow(NotExistChannelException::new);
        return myCarRepository.findAllByKeywordAndChannel(page, keyword, channel).map(MyCarSimple::of);
    }

    @Transactional
    @Override
    public Boolean createMyCarBoard(BoardCreateRequest request, UUID memberId) {
        var req = (MyCarCreateRequest) request;
        Member member = memberRepository.getOrThrow(memberId);
        BoardType boardType = BoardType.convertToEnum(request.getBoardType());

        List<League> mentionedLeagues = leagueRepository.findAllByNameIn(req.getMentionedLeagueNames());
        if (mentionedLeagues.size() != req.getMentionedLeagueNames().size()) {
            throw new NotExistLeagueException();
        }

        MyCarBoard createdBoard = (MyCarBoard) factoryMap.get(boardType).createBoard(request, member);

        Channel channel = channelRepository
                .findByNameIs(boardType.getName())
                .orElseThrow(NotExistChannelException::new);

        createdBoard.setChannel(channel);
        myCarRepository.save(createdBoard);

        for (League league : mentionedLeagues) {
            mentionRepository.save(
                    Mention.builder()
                            .board(createdBoard)
                            .league(league)
                            .build()
            );
        }
        return true;
    }

    @Override
    @Transactional
    public MyCarDetail findMyCarDetail(UUID boardId, ContextMember nullableMember) {
        MyCarBoard myCarBoard = myCarRepository.findByIdAndStatus(boardId, ActivateStatus.ACTIVE)
                .orElseThrow(NotExistBoardException::new);

        List<Mention> mentionedLeagues = mentionRepository.findAllByBoard(myCarBoard);
        myCarBoard.increaseViewCount();
        boolean isLiked = false;
        if (Objects.nonNull(nullableMember)) {
            isLiked = isLike(nullableMember.getId(), myCarBoard);
        }

        return MyCarDetail.of(myCarBoard, mentionedLeagues, isLiked);
    }

//    @Transactional
//    @Override
//    public void increaseView(UUID boardId) {
//        MyCarBoard myCarBoard = myCarRepository.findByIdAndStatus(boardId, ActivateStatus.ACTIVE)
//                .orElseThrow(NotExistBoardException::new);
//
//        myCarBoard.increaseCommentCount();
//    }

    private boolean isLike(UUID memberId, Board board) {
        var member = memberRepository.getOrThrow(memberId);
        return likeRepository.existsByMemberAndBoard(member, board);
    }


}
