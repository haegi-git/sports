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
import Map from '../../Components/Map/Map';
import { useSelector } from "react-redux";

function BoardDetail(){
    const [detailBoard,setDetailBoard] = useState(null);
    const {id} = useParams()
    const navigate = useNavigate();

    const [comment,setComment] = useState('');
    const [commentData, setCommentData] = useState()

    const [chatRoomId,setChatRoomId] = useState();

    const currentUserId = useSelector((state) => state.auth.user?.userId);
    const userRole = useSelector((state) => state.auth.user?.role);

    console.log(userRole)

    useEffect(() => {
        fetchData(`/board/${id}`).then((res)=>{
            setDetailBoard(res.data);
        })
        fetchData(`/comment/get/board/${id}`).then((res)=>{
            setCommentData(res.data.commentItems)
        }).catch((err)=>{
            console.log("불러 올 댓글 없어")
        })
        fetchData(`/board/${id}/chatroom`).then((res)=>{
            if(res){
                setChatRoomId(res.data.id)
            }
        }).catch(()=>{
            console.log("Chat Room 없음");
        })
    }, []);
    const formattedDate = detailBoard?.createdAt.split('T')[0].substring(2);


    const onDelete = async () =>{

        const userConfirmed = window.confirm("정말로 삭제하시겠습니까?");
        if (!userConfirmed) return;

        try{
            await deleteTokenData(`/board/${id}`).then((res)=>{
                alert('게시글이 성공적으로 삭제되었습니다.');
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
            });
        } catch (e) {
            console.log(e);
        }
    }
    const onLike = async () => {
        try {
            // 게시글에 대해 좋아요 요청
            const response = await postTokenJsonData(`/board/${id}/like`, { id });
            const { likeCount } = response;

            // 좋아요 수 갱신
            setDetailBoard(prevDetailBoard => ({
                ...prevDetailBoard,
                likes: likeCount
            }));

        } catch (err) {
            console.error("좋아요 요청 중 오류가 발생했습니다:", err);
        }
    };


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
                    <DateText>작성자 {detailBoard.author} | 작성일 {formattedDate} </DateText>
                </TitleSection>
                {/* 본문 */}
                <ContentSection>
                    <div dangerouslySetInnerHTML={{ __html: detailBoard.content }}></div>

                    {/* 지도 섹션 */}
                    {detailBoard.latitude && detailBoard.longitude && (
                        <Map
                          latitude={detailBoard.latitude}
                          longitude={detailBoard.longitude}
                          isSearchEnabled={false}
                        />
                    )}

                    <InteractionSection>
                        <LikeButton onClick={onLike}>좋아요 {detailBoard.likes}</LikeButton>


                        <div>
                            {(detailBoard.userId === currentUserId || userRole === "ROLE_ADMIN") && (
                                <>
                                    <LikeButton onClick={() => navigate(`/board/update/${id}`)}>수정하기</LikeButton>
                                    <LikeButton onClick={onDelete}>삭제하기</LikeButton>
                                </>
                            )}
                            <ViewsText>조회수 : {detailBoard.views}</ViewsText>
                        </div>

                    </InteractionSection>
                </ContentSection>

                {/* 댓글 섹션 */}
                <Comment
                    comment={comment}
                    setComment={setComment}
                    onCreateComment={onCreateComment}
                    commentData={commentData}
                    setCommentData={setCommentData}
                    postAuthorId={detailBoard.userId}
                    chatRoomId={chatRoomId}/>

            </PageContainer>
        )
    }

}
export default BoardDetail;