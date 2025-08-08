<template>
  <div class="comments-container">
    <div class="comments-header">
      <h3>Comentarios</h3>
      <span class="comments-count">{{ comments.length }} comentarios</span>
    </div>
    
    <div v-if="replyComment" class="reply-info">
      <div class="reply-badge">
        <i class="fas fa-reply"></i>
        Respondiendo a <strong>{{ replyComment.user.name }}</strong>
      </div>
      <button @click="cancelReply" class="cancel-reply-btn">
        <i class="fas fa-times"></i>
      </button>
    </div>
    
    <div class="comments-list" v-if="comments.length">
      <CommentItem
        v-for="comment in topLevelComments"
        :key="comment.idComments"
        :comment="comment"
        :all-comments="comments"
        @reply="setReply"
      />
    </div>
    
    <div v-else class="no-comments">
      <i class="fas fa-comments comments-icon"></i>
      <p>No hay comentarios aún. Sé el primero en comentar.</p>
    </div>
    
    <div class="comment-form" v-if="isLoggedIn">
      <div class="input-container">
        <textarea 
          v-model="newCommentText" 
          placeholder="Escribe un comentario..." 
          rows="2"
          @keyup.ctrl.enter="addComment"
        ></textarea>
        <small class="input-hint">Presiona Ctrl+Enter para enviar</small>
      </div>
      <button 
        @click="addComment" 
        :disabled="!newCommentText.trim()" 
        class="comment-btn"
      >
        <i class="fas fa-paper-plane"></i>
        Comentar
      </button>
    </div>
    
    <div v-else class="login-prompt">
      <p>Inicia sesión para dejar un comentario</p>
      <router-link to="/login" class="login-link">
        <i class="fas fa-sign-in-alt"></i>
        Iniciar Sesión
      </router-link>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from "@/stores/userStore";
import CommentItem from './CommentItem.vue';
import ApiService from '../services/ApiService';


export default {
  name: 'ProductComments',
  components: { CommentItem },
  props: {
    initialComments: {
      type: Array,
      default: () => []
    }
  },
  setup(props) {
    const userStore = useUserStore();
    const route = useRoute();
    const routeId = route.params.id;
    const comments = ref(props.initialComments);
    const newCommentText = ref('');
    const replyComment = ref(null);
    const isLoggedIn = computed(() => {
      return !!(userStore.user && userStore.user.idUser);
    });

    onMounted(async () => {
      try {
        const res = await fetch(ApiService.getPharmacyApiUrl("/comments"));
        if (res.ok) {
          const data = await res.json();
          console.log('Comentarios obtenidos:', data);
          comments.value = data;
        }
      } catch (error) {
        console.error('Error al obtener comentarios', error);
      }
    });

    const topLevelComments = computed(() => {
      const filtered = comments.value.filter(c =>
        !c.prevComment && c.medicine && Number(c.medicine.idMedicine) === Number(routeId)
      );
      console.log('Top level comments filtrados por producto:', filtered);
      return filtered;
    });

    const addComment = async () => {
      if(newCommentText.value.trim()) {
        const payload = {
          user: userStore.user,
          commentText: newCommentText.value,
          medicine: { idMedicine: Number(routeId) }
        };

        if (replyComment.value) {
          payload.prevComment = replyComment.value;
        }

        try {
          const res = await fetch(ApiService.getPharmacyApiUrl("/comments"), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          });
          if(res.ok) {
            const newComment = await res.json();
            comments.value.push(newComment);
            newCommentText.value = '';
            replyComment.value = null;
          } else {
            console.error('Error al crear comentario');
            console.log(JSON.stringify((payload)))
          }
        } catch(error) {
          console.error('Error en la petición POST', error);
        }
      }
    };

    const setReply = (comment) => {
      replyComment.value = comment;
      // Hacer scroll al área de comentarios
      setTimeout(() => {
        document.querySelector('.comment-form textarea')?.focus();
      }, 100);
    };

    const cancelReply = () => {
      replyComment.value = null;
    };

    return {
      comments,
      topLevelComments,
      newCommentText,
      addComment,
      replyComment,
      setReply,
      cancelReply,
      isLoggedIn
    };
  }
};
</script>

<style scoped>
.comments-container {
  margin-top: 2rem;
  background-color: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #eaeaea;
  padding-bottom: 0.8rem;
}

.comments-header h3 {
  font-size: 1.2rem;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.comments-count {
  background-color: #e9ecef;
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.9rem;
  color: #495057;
}

.reply-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f0f7ff;
  padding: 0.7rem 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  border-left: 3px solid #3498db;
}

.reply-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #4a6fa5;
}

.reply-badge i {
  font-size: 0.8rem;
}

.cancel-reply-btn {
  background: none;
  border: none;
  color: #6c757d;
  cursor: pointer;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.cancel-reply-btn:hover {
  background-color: #e2e6ea;
  color: #495057;
}

.comments-list {
  margin-bottom: 2rem;
}

.no-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  color: #6c757d;
  margin-bottom: 1.5rem;
}

.comments-icon {
  font-size: 3rem;
  color: #dee2e6;
  margin-bottom: 1rem;
}

.comment-form {
  margin-top: 1.5rem;
  border-top: 1px solid #eaeaea;
  padding-top: 1.5rem;
}

.input-container {
  position: relative;
  margin-bottom: 1rem;
}

.input-hint {
  position: absolute;
  right: 10px;
  bottom: 5px;
  font-size: 0.8rem;
  color: #adb5bd;
}

textarea {
  width: 100%;
  padding: 0.8rem;
  margin-bottom: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-family: inherit;
  font-size: 0.95rem;
  resize: vertical;
  transition: border-color 0.2s;
}

textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.comment-btn {
  padding: 0.8rem 1.5rem;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: background-color 0.2s;
}

.comment-btn:hover:not(:disabled) {
  background-color: #2980b9;
}

.comment-btn:disabled {
  background-color: #b3b3b3;
  cursor: not-allowed;
}

.login-prompt {
  background-color: #f8f9fa;
  padding: 1.2rem;
  border-radius: 8px;
  text-align: center;
  margin-top: 1.5rem;
}

.login-link {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.8rem;
  padding: 0.6rem 1.2rem;
  background-color: #3498db;
  color: white;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: background-color 0.2s;
}

.login-link:hover {
  background-color: #2980b9;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .comments-container {
    padding: 1rem;
  }
  
  .comments-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .comments-count {
    align-self: flex-start;
  }
}
</style>