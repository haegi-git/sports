import {useEffect, useState} from "react";
import {
    ContentSection,
    DateText,
    InteractionSection,
    LikeButton,
    PageContainer,
    Title,
    TitleSection,
    ViewsText
} from "../../styled/Board/BoardPageStyled";
import {fetchData} from "../../Server/ApiServiceNoToken";
import {useNavigate, useParams} from "react-router-dom";
import {deleteTokenData, postTokenJsonData} from "../../Server/ApiService";
import Comment from "../../Components/Comment";

function BoardDetail(){
    const [detailBoard,setDetailBoard] = useState(null);
    const {id} = useParams()
    const navigate = useNavigate();

    const [comment,setComment] = useState('');
    const [commentData, setCommentData] = useState()

    useEffect(() => {
        fetchData(`/board/${id}`).then((res)=>{
            setDetailBoard(res.data);
        })
        fetchData(`/comment/get/board/${id}`).then((res)=>{
            setCommentData(res.data.commentItems)
        }).catch((err)=>{
            console.log("불러 올 댓글 없어")
        })
    }, []);
    const formattedDate = detailBoard?.createdAt.split('T')[0].substring(2);


    const onDelete = async () =>{
        try{
            await deleteTokenData(`/board/${id}`).then((res)=>{
                navigate('/board')
            })
        }catch(e){
            console.log(e)
        }
    }

    const onCreateComment = async () => {
        const postComment = {
            content: comment,
        };
        try {
            await postTokenJsonData(`/comment/add/board/${id}`, postComment);
            setComment(''); // 댓글 입력란 초기화
            // 댓글 작성 후 새로 데이터를 가져와서 반영
            fetchData(`/comment/get/board/${id}`).then((res) => {
                setCommentData(res.data.commentItems);
            }).catch((err) => {
                console.log("댓글 불러오기 실패");
            });
        } catch (e) {
            console.log(e);
        }
    }
    const onLike = async () => {
        const likeId = {
            id: id,
        };
        try {
            await postTokenJsonData(`/board/${id}/like`, likeId);
            // 성공적으로 좋아요를 눌렀다면, 좋아요 수를 증가시킴
            setDetailBoard(prevDetailBoard => ({
                ...prevDetailBoard,
                likes: prevDetailBoard.likes + 1
            }));
        } catch (err) {
            console.log(err);
        }
    }


    if(detailBoard === null){
        return(
            <div>로딩중</div>
        )
    }else{
        return(
            <PageContainer>
                {/* 제목 및 작성 날짜 */}
                <TitleSection>
                    <Title>{detailBoard.title}</Title>
                    <DateText>작성자:{detailBoard.author}  | 작성일: {formattedDate} </DateText>
                </TitleSection>
                {/* 본문 */}
                <ContentSection>
                    <div dangerouslySetInnerHTML={{ __html: detailBoard.content }}></div>
                    <InteractionSection>
                        <LikeButton onClick={onLike}>좋아요 {detailBoard.likes}</LikeButton>
                        <div>
                            <LikeButton
                            onClick={()=>navigate(`/board/update/${id}`)}>수정하기</LikeButton>
                            <LikeButton onClick={onDelete}>삭제하기</LikeButton>
                            <ViewsText>조회수: {detailBoard.views}</ViewsText>
                        </div>
                    </InteractionSection>
                </ContentSection>

                {/* 댓글 섹션 */}
                <Comment
                    comment={comment}
                    setComment={setComment}
                    onCreateComment={onCreateComment}
                    commentData={commentData}
                    setCommentData={setCommentData}/>
            </PageContainer>
        )
    }

}
export default BoardDetail;