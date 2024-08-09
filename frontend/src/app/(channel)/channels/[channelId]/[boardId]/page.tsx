"use client";

import React, { useState, useEffect, useCallback } from "react";
import styled from "styled-components";
import { FaRegHeart, FaHeart } from "react-icons/fa";
import BoardDetailTitle from "@/components/channel/board/BoardDetailTitle";
import { useAuthStore } from "@/store/authStore";
import { PostDetail } from "@/types/channelType";
import { fetchComment } from "@/types/commentTypes";
import { fetchChannelPostDetail, fetchBoastDetail } from "@/api/channel";
import CommentList from "@/components/common/UI/comment/CommentList";
import { fetchCommentList } from "@/api/comment";
import { fetchChannelLike, fetchChannelLikeDelete } from "@/api/board";
import { formatPostDate } from "@/utils/formatPostDate";

export default function ChannelBoardDetailPage({
  params,
}: {
  params: { channelId: string; boardId: string };
}) {
  const boastId = process.env.NEXT_PUBLIC_BOAST_ID;
  const channelId = params.channelId;
  const boardId = params.boardId;

  const [boardDetail, setBoardDetail] = useState<PostDetail | null>(null);
  const [commentList, setCommentList] = useState<fetchComment | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { isLoggedIn, user } = useAuthStore();

  const [isLiked, setIsLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);

  const toggleLike = async () => {
    if (isLiked) {
      const likeData = await fetchChannelLikeDelete(boardId);
      setLikeCount(likeData.likeCount);
      setIsLiked(likeData.isLike);
    } else {
      const likeData = await fetchChannelLike(boardId);
      setLikeCount(likeData.likeCount);
      setIsLiked(likeData.isLike);
    }
  };

  const loadBoardDetail = useCallback(async () => {
    try {
      let details;
      if (boastId === channelId) {
        details = await fetchBoastDetail(boardId);
      } else {
        details = await fetchChannelPostDetail(boardId, channelId);
      }

      setBoardDetail(details);
      setLikeCount(details.likeCount);
      setIsLiked(details.liked);
    } catch (error) {
      console.error(error);
      setError("Failed to load post details. Please try again later.");
    }
  }, [boardId, channelId]);

  const loadCommentDetail = useCallback(async () => {
    if (!isLoggedIn) return;

    try {
      const fetchCommentsList = await fetchCommentList(boardId);
      setCommentList(fetchCommentsList);
    } catch (error) {
      console.log(error);
    }
  }, [boardId, isLoggedIn]);

  const handleCommentAdded = async () => {
    try {
      // 새로운 댓글을 가져와서 업데이트합니다.
      const fetchCommentsList = await fetchCommentList(boardId);
      if (fetchCommentsList) {
        // 새 댓글이 추가될 때 기존 댓글 리스트에 추가합니다.
        setCommentList(fetchCommentsList);
      }
    } catch (error) {
      console.error("Failed to update comment list:", error);
    }
  };

  useEffect(() => {
    loadBoardDetail();
    loadCommentDetail();
  }, [loadBoardDetail, loadCommentDetail]);

  if (!boardDetail) {
    return <div>{error ? error : "Loading..."}</div>;
  }

  return (
    <>
      <BoardDetailTitle
        title={boardDetail.title}
        createdAt={formatPostDate(boardDetail.createdAt)}
        viewCount={boardDetail.viewCount}
        likeCount={likeCount}
        member={boardDetail.member}
        tags={boardDetail.mentionedLeagues}
        boardId={boardDetail.id}
        channelId={channelId}
      />
      <Content dangerouslySetInnerHTML={{ __html: boardDetail.content }} />
      <CommentContainer>
        <WriterContainer>
          <HeartButton onClick={toggleLike} $isLiked={isLiked}>
            {isLiked ? <FaHeart /> : <FaRegHeart />}
          </HeartButton>
        </WriterContainer>
        <CommentList
          comments={commentList?.comments || []}
          commentCount={commentList?.commentCount || 0}
          boardId={boardId}
          leagueId=""
          isLeague={false}
          onCommentAdded={handleCommentAdded}
          boardAuthor={boardDetail.member.nickname}
        />
      </CommentContainer>
    </>
  );
}


const CommentContainer = styled.div`
  display: flex;
  flex-direction: column;
  border-top: 1px solid #bebebe;
`;

const WriterContainer = styled.div`
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
`;

const HeartButton = styled.button<{ $isLiked: boolean }>`
  margin: 5px 0px 2px 0px;
  padding: 0;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  color: ${({ $isLiked }) => ($isLiked ? "#d60606" : "#333")};
  display: flex;
  justify-content: center;
  align-items: center;

  &:hover {
    color: ${({ $isLiked }) => ($isLiked ? "#ff6666" : "#d60606")};
  }

  @media (min-width: 768px) {
    font-size: 20px;
  }
`;

const Content = styled.div`
  font-size: 16px;
  line-height: 1.5;
  color: #333;
  padding: 16px 16px 40px 16px;
  border-top: 1px solid #bebebe;

  @media (min-width: 480px) {
    font-size: 17px;
    padding: 20px 20px 50px 20px;
  }
`;
