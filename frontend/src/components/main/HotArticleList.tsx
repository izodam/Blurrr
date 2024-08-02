import styled from "styled-components";
import HotArticleListItem from "@/components/main/HotArticleListItem";
import { HotBoardItem } from "@/types/mainPageTypes";

interface HotArticleListProps {
  hotBoards: HotBoardItem[];
}

const HotArticleList = ({ hotBoards }: HotArticleListProps) => {
  return (
    <ArticleList>
      {hotBoards.map((board: HotBoardItem) => (
        <HotArticleListItem
          key={board.id}
          id={board.id}
          channel={board.channel}
          title={board.title}
          likeCount={board.likeCount}
          commentCount={board.commentCount}
        />
      ))}
    </ArticleList>
  );
};
const ArticleList = styled.div``;

export default HotArticleList;
