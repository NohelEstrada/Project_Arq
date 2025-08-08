<template>
  <div class="comment-item">
    <div class="comment-content">
      <div class="comment-header">
        <div class="user-info">
          <div class="user-avatar">
            {{ getInitials(comment.user ? comment.user.name : 'Anónimo') }}
          </div>
          <span class="user-name">{{ comment.user ? comment.user.name : 'Anónimo' }}</span>
        </div>
        <div class="comment-actions">
          <button @click="$emit('reply', comment)" class="reply-btn">
            <i class="fas fa-reply"></i>
            Responder
          </button>
        </div>
      </div>
      <div class="comment-text">
        {{ comment.commentText }}
      </div>
    </div>
    
    <div class="replies" v-if="childComments.length">
      <CommentItem
        v-for="child in childComments"
        :key="child.idComments"
        :comment="child"
        :all-comments="allComments"
        @reply="$emit('reply', $event)"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'CommentItem',
  props: {
    comment: { type: Object, required: true },
    allComments: { type: Array, required: true }
  },
  methods: {
    getInitials(name) {
      if (!name) return '?';
      return name
        .split(' ')
        .map(word => word[0])
        .slice(0, 2)
        .join('')
        .toUpperCase();
    }
  },
  computed: {
    childComments() {
      return this.allComments.filter(
        c => c.prevComment && c.prevComment.idComments === this.comment.idComments
      );
    }
  }
};
</script>

<style scoped>
.comment-item {
  position: relative;
  padding-left: 20px;
  margin-bottom: 1.5rem;
}

.comment-item::before {
  content: '';
  position: absolute;
  top: 30px;
  left: 0;
  bottom: 0;
  width: 2px;
  background-color: #e9ecef;
}

.comment-content {
  background-color: #f8f9fa;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.8rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background-color: #3498db;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
}

.user-name {
  font-weight: 600;
  color: #2c3e50;
}

.comment-actions {
  display: flex;
  gap: 0.5rem;
}

.reply-btn {
  background: none;
  border: none;
  color: #6c757d;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  transition: all 0.2s;
}

.reply-btn:hover {
  background-color: #e9ecef;
  color: #495057;
}

.reply-btn i {
  font-size: 0.8rem;
}

.comment-text {
  color: #333;
  line-height: 1.5;
  white-space: pre-line;
}

.replies {
  margin-left: 1.5rem;
  margin-top: 0.5rem;
}

/* Estilos para niveles anidados */
.replies .comment-item {
  margin-bottom: 1rem;
}

.replies .comment-content {
  background-color: #f0f7ff;
}

.replies .user-avatar {
  background-color: #2980b9;
}

/* Cambios para niveles más profundos */
.replies .replies .comment-content {
  background-color: #e8f5e9;
}

.replies .replies .user-avatar {
  background-color: #388e3c;
}

.replies .replies .replies .comment-content {
  background-color: #fff3e0;
}

.replies .replies .replies .user-avatar {
  background-color: #f57c00;
}

/* Responsive */
@media (max-width: 576px) {
  .comment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .comment-actions {
    align-self: flex-end;
  }
}
</style>