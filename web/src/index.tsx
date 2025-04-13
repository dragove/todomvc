import {
	createMemo,
	onCleanup,
	createResource,
	createEffect,
	Show,
	For,
	createSignal,
} from "solid-js";
import { createStore } from "solid-js/store";
import { render } from "solid-js/web";

type Todo = {
	id: number;
	title: string;
	completed: boolean;
};

type Filter = "all" | "active" | "completed";

type TodoStore = {
	counter: number;
	todos: Todo[];
	showMode: Filter;
	editingTodoId: number | undefined;
};

declare module "solid-js" {
	namespace JSX {
		interface Directives {
			setFocus: boolean;
		}
	}
}

const baseUrl = "http://127.0.0.1:8080";

async function fetchTodos(filter: string | null): Promise<Todo[]> {
	const res = await fetch(`${baseUrl}/all?filter=${filter}`);
	const data = await res.json();
	return data as Todo[];
}
async function toggleAllTodos(completed: boolean) {
	await fetch(`${baseUrl}/toggleAll?completed=${completed}`, {
		method: "PUT",
	});
}
async function deleteTodo(id: number) {
	await fetch(`${baseUrl}/delete?id=${id}`, {
		method: "DELETE",
	});
}
async function deleteCompleted() {
	await fetch(`${baseUrl}/clear`, {
		method: "DELETE",
	});
}
async function createTodo(todo: Partial<Todo>) {
	await fetch(`${baseUrl}/add`, {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(todo),
	});
}
async function updateTodo(todo: Partial<Todo>) {
	await fetch(`${baseUrl}/update`, {
		method: "PUT",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify(todo),
	});
}

const TodoApp = () => {
	const [filter, setFilter] = createSignal<Filter>("all");
	const [todos, { refetch }] = createResource(filter, fetchTodos);
	const [state, setState] = createStore<TodoStore>({
		counter: 1,
		todos: [],
		showMode: filter(),
		editingTodoId: undefined,
	});
	createEffect(() => {
		setState({
			counter: (todos()?.length ?? 0) + 1,
			todos: todos() ?? [],
			showMode: filter(),
		});
	});
	const remainingCount = createMemo(
		() =>
			state.todos.length - state.todos.filter((todo) => todo.completed).length,
	);
	const removeTodo = async (todoId: number) => {
		await deleteTodo(todoId);
		await refetch(filter());
	};
	const editTodo = async (todo: Partial<Todo>) => {
		await updateTodo(todo);
		await refetch(filter());
	};
	const clearCompleted = async () => {
		await deleteCompleted();
		await refetch(filter());
	};
	const toggleAll = async (completed: boolean) => {
		await toggleAllTodos(completed);
		await refetch(filter());
	};
	const setEditing = (todoId?: number) => setState("editingTodoId", todoId);
	const addTodo = async ({ target, code }: KeyboardEvent) => {
		const title = (target as HTMLInputElement).value.trim();
		if (code === "Enter" && title) {
			await createTodo({ title, completed: false });
			await refetch(filter());
			(target as HTMLInputElement).value = "";
		}
	};
	const save = async (
		todoId: number,
		{ target: { value } }: { target: HTMLInputElement },
	) => {
		const title = value.trim();
		if (state.editingTodoId === todoId && title) {
			await editTodo({ id: todoId, title });
			setEditing();
		}
	};
	const toggle = (
		todoId: number,
		{ target: { checked } }: { target: HTMLInputElement },
	) => editTodo({ id: todoId, completed: checked });
	const doneEditing = (todoId: number, e: KeyboardEvent) => {
		if (e.code === "Enter") save(todoId, e as any);
		else if (e.code === "Escape") setEditing();
	};

	const locationHandler = () =>
		setFilter((location.hash.slice(2) as Filter) || "all");
	window.addEventListener("hashchange", locationHandler);
	onCleanup(() => window.removeEventListener("hashchange", locationHandler));

	return (
		<section class="todoapp">
			<header class="header">
				<h1>todos</h1>
				<input
					class="new-todo"
					placeholder="What needs to be done?"
					onKeyDown={addTodo}
				/>
			</header>

			<section class="main">
				<input
					id="toggle-all"
					class="toggle-all"
					type="checkbox"
					checked={!remainingCount()}
					onInput={({ target: { checked } }) => toggleAll(checked)}
				/>
				<label for="toggle-all" />
				<ul class="todo-list">
					<For each={state.todos}>
						{(todo) => (
							<li
								class="todo"
								classList={{
									editing: state.editingTodoId === todo.id,
									completed: todo.completed,
								}}
							>
								<div class="view">
									<input
										class="toggle"
										type="checkbox"
										checked={todo.completed}
										onInput={[toggle, todo.id]}
									/>
									<label onDblClick={[setEditing, todo.id]}>{todo.title}</label>
									<button class="destroy" onClick={[removeTodo, todo.id]} />
								</div>
								<Show when={state.editingTodoId === todo.id}>
									<input
										class="edit"
										value={todo.title}
										onFocusOut={[save, todo.id]}
										onKeyUp={[doneEditing, todo.id]}
										use:setFocus
									/>
								</Show>
							</li>
						)}
					</For>
				</ul>
			</section>

			<footer class="footer">
				<span class="todo-count">
					<strong>{remainingCount()}</strong>{" "}
					{remainingCount() === 1 ? " item " : " items "} left
				</span>
				<ul class="filters">
					<li>
						<a href="#/" classList={{ selected: state.showMode === "all" }}>
							All
						</a>
					</li>
					<li>
						<a
							href="#/active"
							classList={{ selected: state.showMode === "active" }}
						>
							Active
						</a>
					</li>
					<li>
						<a
							href="#/completed"
							classList={{ selected: state.showMode === "completed" }}
						>
							Completed
						</a>
					</li>
				</ul>
				<Show when={remainingCount() !== state.todos.length}>
					<button class="clear-completed" onClick={clearCompleted}>
						Clear completed
					</button>
				</Show>
			</footer>
		</section>
	);
};

render(TodoApp, document.getElementById("root")!);
